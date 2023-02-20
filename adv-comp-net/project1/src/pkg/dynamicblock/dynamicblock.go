package dynamicblock

import (
	"crypto/sha256"
	"fmt"
	"hash"
	"proxyserver/pkg/cache"
	"sync"
)

type DynamicBlock struct {
	RemoteAddr string `json:"remote_addr"`
	Method     string `json:"method"`
	Url        string `json:"url"`
	Blocked    bool   `json:"blocked"`
}

type UrlList struct {
	hash     hash.Hash
	UrlVals  map[string]DynamicBlock `json:"url_values"`
	busyVals map[string]*sync.Mutex
	mutex    *sync.Mutex
}

func New() *UrlList {
	return &UrlList{
		sha256.New(),
		make(map[string]DynamicBlock),
		make(map[string]*sync.Mutex),
		&sync.Mutex{},
	}
}

func (l *UrlList) release(hash string, lsting DynamicBlock) {
	l.mutex.Lock()
	delete(l.busyVals, hash)
	l.UrlVals[hash] = lsting
	l.mutex.Unlock()
}

func (l *UrlList) Has(key string) (*sync.Mutex, bool) {
	hash := cache.CalcHash(key)

	l.mutex.Lock()
	defer l.mutex.Unlock()

	if lock, busy := l.busyVals[hash]; busy {
		l.mutex.Unlock()
		lock.Lock()
		lock.Unlock()
		l.mutex.Lock()
	}

	if _, found := l.UrlVals[hash]; found {
		return nil, true
	}

	lock := new(sync.Mutex)
	lock.Lock()
	l.busyVals[hash] = lock
	return lock, false
}

func (l *UrlList) Get(key string) (*DynamicBlock, error) {
	hash := cache.CalcHash(key)

	l.mutex.Lock()
	url, ok := l.UrlVals[hash]
	l.mutex.Unlock()

	if !ok {
		return nil, fmt.Errorf("request url item not logged")
	}

	return &url, nil
}

func (l *UrlList) Set(key string, lsting *DynamicBlock) error {
	hash := cache.CalcHash(key)

	defer l.release(hash, *lsting)
	return nil
}

func (l *UrlList) Block(key string) (*DynamicBlock, error) {
	hash := cache.CalcHash(key)

	l.mutex.Lock()
	lsting, ok := l.UrlVals[hash]
	l.mutex.Unlock()

	if !ok {
		return nil, fmt.Errorf("%s (%s) not found", key, hash)
	}
	lsting.Blocked = true
	defer l.release(hash, lsting)
	return &lsting, nil
}

func (l *UrlList) Unblock(key string) (*DynamicBlock, error) {
	hash := cache.CalcHash(key)

	l.mutex.Lock()
	lsting, ok := l.UrlVals[hash]
	l.mutex.Unlock()

	if !ok {
		return nil, fmt.Errorf("%s (%s) not found", key, hash)
	}
	lsting.Blocked = false
	defer l.release(hash, lsting)
	return &lsting, nil
}
