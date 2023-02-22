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
	KnownValues  map[string][]byte        `json:"known_vals"`
	TimingValues map[string]time.Duration `json:"timing_vals"`
	busyValues   map[string]*sync.Mutex
	mutex        *sync.Mutex

	lg *logrus.Logger
}

func New(path string, lg *logrus.Logger) (*Cache, error) {
	os.RemoveAll(path)
	os.MkdirAll(path, os.ModePerm)

	cache := &Cache{
		folder:       path,
		hash:         sha256.New(),
		KnownValues:  make(map[string][]byte),
		TimingValues: make(map[string]time.Duration),
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

	if _, found := c.KnownValues[hash]; found {
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
	content, ok := c.KnownValues[hashVal]
	timing := c.TimingValues[hashVal]
	cLen := int64(0)
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
		stat, err := file.Stat()
		if err != nil {
			return nil, fmt.Errorf("error getting size of file %s, %s", hashVal, err)
		}
		cLen = stat.Size()
	} else {
		response = bytes.NewReader(content)
	}

	c.lg.Infof("saved %dms and fetching %d bytes", timing.Milliseconds(), cLen)
	return &response, nil
}

// internal function to handling blocking of content being added or updated
func (c *Cache) release(hashValue string, content []byte, timing time.Duration) {
	c.mutex.Lock()
	delete(c.busyValues, hashValue)
	c.KnownValues[hashValue] = content
	c.TimingValues[hashValue] = timing
	c.mutex.Unlock()
}

func (c *Cache) Put(key string, content *io.Reader, contentLength uint64, timing time.Duration) error {
	hashVal := cache.CalcHash(key)

	defer c.release(hashVal, nil, timing)
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
