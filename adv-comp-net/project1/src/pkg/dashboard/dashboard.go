package dashboard

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"path"
	"proxyserver"

	"github.com/sirupsen/logrus"
)

type Dashboard struct {
	lg *logrus.Logger
	p  *proxyserver.ProxyServer
}

func New(lg *logrus.Logger, p *proxyserver.ProxyServer) *Dashboard {
	return &Dashboard{
		lg,
		p,
	}
}

func (d *Dashboard) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	switch r.Method {
	case "GET":
		switch r.URL.EscapedPath() {
		case "/urls":
			w.Header().Set("Content-Type", "application/json")
			json.NewEncoder(w).Encode(d.p.UrlList.UrlVals)
		default:
			d.lg.Info("GET default")
			wd, err := os.Getwd()
			if err != nil {
				http.Error(w, fmt.Sprintf("couldn't get working dir, %s", err), http.StatusInternalServerError)
				break
			}
			http.ServeFile(w, r, path.Join(wd, "pkg/dashboard", "./index.html"))
		}
	case "POST":
		body, err := io.ReadAll(r.Body)
		if err != nil {
			fmt.Fprintf(w, "Error occurred reading body: %s", err)
		}
		switch r.URL.EscapedPath() {
		case "/block":
			d.p.UrlList.Block(string(body))
			json.NewEncoder(w).Encode(d.p.UrlList.UrlVals[string(body)])
		case "/unblock":
			d.p.UrlList.Unblock(string(body))
			json.NewEncoder(w).Encode(d.p.UrlList.UrlVals[string(body)])
		}
	default:
		fmt.Fprintf(w, "Sorry, only GET and POST methods are supported")
	}
}
