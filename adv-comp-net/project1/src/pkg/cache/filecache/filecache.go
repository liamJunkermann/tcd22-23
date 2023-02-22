package filecache

import (
	"bufio"
	"bytes"
	"crypto/sha256"
	"fmt"
	"hash"
	"io"
	"os"
	"path"
	"proxyserver/pkg/cache"
	"sync"
	"time"

	"github.com/sirupsen/logrus"
)

type Cache struct {
	folder       string
	hash         hash.Hash
	knownValues  map[string][]byte
	timingValues map[string]time.Duration
	busyValues   map[string]*sync.Mutex
	mutex        *sync.Mutex

	lg *logrus.Logger
}

func New(path string, lg *logrus.Logger) (*Cache, error) {
	dir, err := os.ReadDir(path)
	if err != nil {
		lg.Debugf("couldn't open cache folder %s creating new folder", err)
		err := os.MkdirAll(path, os.ModePerm)
		if err != nil {
			return nil, fmt.Errorf("could not create cache folder %s, %s", path, err)
		}
	}

	values := make(map[string][]byte)
	// priming values with directory content
	for _, info := range dir {
		if !info.IsDir() {
			values[info.Name()] = nil
		}
	}

	cache := &Cache{
		folder:       path,
		hash:         sha256.New(),
		knownValues:  values,
		timingValues: make(map[string]time.Duration),
		busyValues:   make(map[string]*sync.Mutex),
		mutex:        &sync.Mutex{},
		lg:           lg,
	}

	return cache, nil
}

func (c *Cache) Has(key string) (*sync.Mutex, bool) {
	hash := cache.CalcHash(key)

	c.mutex.Lock()
	defer c.mutex.Unlock()

	if lock, busy := c.busyValues[hash]; busy {
		c.mutex.Unlock()
		lock.Lock()
		lock.Unlock()
		c.mutex.Lock()
	}

	if _, found := c.knownValues[hash]; found {
		return nil, true
	}

	lock := new(sync.Mutex)
	lock.Lock()
	c.busyValues[hash] = lock
	return lock, false
}

func (c *Cache) Get(key string) (*io.Reader, error) {
	var response io.Reader
	hashVal := cache.CalcHash(key)

	c.mutex.Lock()
	content, ok := c.knownValues[hashVal]
	timing := c.timingValues[hashVal]
	c.mutex.Unlock()
	if !ok && len(content) > 0 {
		return nil, fmt.Errorf("key '%s' (%s) not in cache", key, hashVal)
	}

	c.lg.Debugf("cache has key %s (%s)", key, hashVal)

	if content == nil {
		c.lg.Debugf("loading %s from file", key)

		file, err := os.Open(path.Join(c.folder, hashVal))
		if err != nil {
			return nil, fmt.Errorf("error opening cache file %s, %s", hashVal, err)
		}

		response = file
	} else {
		response = bytes.NewReader(content)
	}

	c.lg.Infof("saved %dms and fetching %d bytes", timing.Milliseconds(), len(content))
	return &response, nil
}

// internal function to handling blocking of content being added or updated
func (c *Cache) release(hashValue string, content []byte, timing time.Duration) {
	c.mutex.Lock()
	delete(c.busyValues, hashValue)
	c.knownValues[hashValue] = content
	c.timingValues[hashValue] = timing
	c.mutex.Unlock()
}

func (c *Cache) Put(key string, content *io.Reader, contentLength uint64, timing time.Duration) error {
	hashVal := cache.CalcHash(key)

	defer c.release(hashVal, nil, time.Since(time.Now()))
	file, err := os.Create(path.Join(c.folder, hashVal))
	if err != nil {
		return fmt.Errorf("could not create file %s, %s", hashVal, err)
	}

	writer := bufio.NewWriter(file)
	b, err := io.Copy(writer, *content)
	if err != nil {
		return fmt.Errorf("could not copy to file %s, %s", hashVal, err)
	}
	c.lg.Debugf("wrote %d bytes to %s", b, path.Join(c.folder, hashVal))
	return nil
}
