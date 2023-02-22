package main

import (
	"flag"
	"fmt"
	"net/http"
	"proxyserver"
	"proxyserver/pkg/cache/filecache"
	"proxyserver/pkg/dashboard"
	"proxyserver/pkg/dynamicblock"

	"github.com/sirupsen/logrus"
)

func main() {
	proxyPort := flag.String("proxy", "8081", "The proxy port")
	webPort := flag.String("web", "8080", "The web dashboard port")
	flag.Parse()

	lg := logrus.New()
	lg.SetLevel(logrus.DebugLevel)

	cache, err := filecache.New("cache", lg)
	if err != nil {
		lg.Fatalf("could not init cache, %s", err)
	}
	urlList := dynamicblock.New()

	proxy := proxyserver.New(lg, urlList, cache)
	dash := dashboard.New(lg, proxy)

	errCh := make(chan error)

	go func() {
		lg.Infof("starting proxy server on :%s", *proxyPort)
		if err := http.ListenAndServe(":"+*proxyPort, proxy); err != nil {
			errCh <- fmt.Errorf("proxy error, %s", err)
		}
	}()

	go func() {
		lg.Infof("starting dash server on :%s", *webPort)
		if err := http.ListenAndServe(":"+*webPort, dash); err != nil {
			errCh <- fmt.Errorf("proxy error, %s", err)
		}
	}()

	fatalErr := <-errCh
	lg.Fatal(fatalErr)
}
