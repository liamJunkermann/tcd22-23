package proxyserver

import (
	"encoding/json"
	"fmt"
	"io"
	"net"
	"net/http"
	"proxyserver/pkg/cache"
	"proxyserver/pkg/dynamicblock"
	"strings"
	"time"

	"github.com/sirupsen/logrus"
)

type ProxyServer struct {
	lg      *logrus.Logger
	UrlList *dynamicblock.UrlList
	c       cache.Cache
}

var hopHeaders = []string{
	"Connection",
	"Keep-Alive",
	"Proxy-Authenticate",
	"Proxy-Authorization",
	"Te", // canonicalized version of "TE"
	"Trailers",
	"Transfer-Encoding",
	"Upgrade",
}

func delHopHeaders(header http.Header) {
	for _, h := range hopHeaders {
		header.Del(h)
	}
}

func appendHostToXForwardHeader(header http.Header, host string) {
	// Including previous proxy hops in X-Forwarded-For Header
	if prior, ok := header["X-Forwarded-For"]; ok {
		host = strings.Join(prior, ", ") + ", " + host
	}
	header.Set("X-Forwarded-For", host)
}

func New(lg *logrus.Logger, urlist *dynamicblock.UrlList, c cache.Cache) *ProxyServer {
	return &ProxyServer{lg, urlist, c}
}

func (p *ProxyServer) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	p.lg.Infof("%s, %s, %s, Host: %s", r.RemoteAddr, r.Method, r.URL, r.Host)
	fullUrl := r.Host + r.URL.EscapedPath() + "?" + r.URL.RawQuery

	_, hasLsting := p.UrlList.Has(fullUrl)
	if !hasLsting {
		err := p.UrlList.Set(fullUrl, &dynamicblock.DynamicBlock{RemoteAddr: r.RemoteAddr, Method: r.Method, Url: r.Host + "" + r.URL.EscapedPath(), Blocked: false})
		if err != nil {
			http.Error(w, fmt.Sprintf("failed to set lsting, %s", err), http.StatusInternalServerError)
			return
		}
	}

	lsting, err := p.UrlList.Get(fullUrl)
	if err != nil {
		if err != nil {
			http.Error(w, fmt.Sprintf("failed to get lsting, %s", err), http.StatusInternalServerError)
			return
		}
	}

	str, _ := json.Marshal(lsting)
	p.lg.Debugf("got listing, %s", string(str))

	if !lsting.Blocked {
		// reqDump, _ := httputil.DumpRequest(r, true)
		if r.Method != "CONNECT" {
			client := &http.Client{}

			if busy, ok := p.c.Has(fullUrl); !ok {
				p.lg.Debugf("cache does not have %s", fullUrl)
				startTime := time.Now()
				defer busy.Unlock()
				r.RequestURI = ""

				delHopHeaders(r.Header)
				if clientIp, _, err := net.SplitHostPort(r.RemoteAddr); err == nil {
					appendHostToXForwardHeader(r.Header, clientIp)
				}

				resp, err := client.Do(r)
				if err != nil {
					http.Error(w, fmt.Sprintf("forwarding error, %s", err), http.StatusInternalServerError)
					return
				}

				reader := io.Reader(resp.Body)
				totalTime := time.Since(startTime)
				p.lg.Debugf("req took %dms", totalTime.Milliseconds())
				err = p.c.Put(fullUrl, &reader, uint64(resp.ContentLength), totalTime)
				if err != nil {
					http.Error(w, fmt.Sprintf("failed to put to cache, %s", err), http.StatusInternalServerError)
					return
				}
				defer resp.Body.Close()
			}

			content, err := p.c.Get(fullUrl)
			if err != nil {
				http.Error(w, fmt.Sprintf("cache error, %s", err), http.StatusInternalServerError)
				return
			} else {
				contentWritten, err := io.Copy(w, *content)
				if err != nil {
					p.lg.Errorf("error writing response: %s", err)
					return
				}
				p.lg.Infof("wrote %d bytes to client", contentWritten)
			}
		} else {
			if !strings.Contains(r.Host, ":") {
				r.Host += ":80"
			}

			srvConn, err := net.DialTimeout("tcp", r.Host, 10*time.Second)
			if err != nil {
				http.Error(w, err.Error(), http.StatusServiceUnavailable)
				return
			}
			p.lg.Debug("created srvConn")

			w.WriteHeader(http.StatusOK)
			hj, ok := w.(http.Hijacker)
			if !ok {
				http.Error(w, "Hijacking not supported", http.StatusInternalServerError)
				return
			}

			clientConn, _, err := hj.Hijack()
			if err != nil {
				http.Error(w, err.Error(), http.StatusServiceUnavailable)
			}
			go transfer(srvConn, clientConn)
			go transfer(clientConn, srvConn)

			p.lg.Debug("here now")
		}
	} else {
		http.Error(w, "Proxy Blocked", http.StatusForbidden)
	}
}

func transfer(destination io.WriteCloser, source io.ReadCloser) {
	defer destination.Close()
	defer source.Close()
	io.Copy(destination, source)
}
