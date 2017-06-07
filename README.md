# music-scrobbler

music scrobbler using last.fm api - built with re-frame + re-agent in clojurescript

## Development Mode

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3669](http://localhost:3669).

## Production Build


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```
