package cache

import (
	"crypto/sha256"
	"encoding/hex"
	"io"
	"sync"
	"time"
)

type Cache interface {
	Get(key string) (*io.Reader, error)
	Put(key string, content *io.Reader, contentLength uint64, expiry time.Duration) error
	Has(key string) (*sync.Mutex, bool)
}

func CalcHash(data string) string {
	sha := sha256.Sum256([]byte(data))
	return hex.EncodeToString(sha[:])
}
