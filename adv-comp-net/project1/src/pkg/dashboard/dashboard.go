package dashboard

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
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
	if r.URL.EscapedPath() == "/urls" && r.Method == "GET" {
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(d.p.UrlList.UrlVals)
	} else {
		switch r.Method {
		case "GET":
			http.ServeFile(w, r, "index.html")
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
}
