# A very opinionated Clojure project template

[![Build Status](https://travis-ci.org/dryewo/cyrus.svg?branch=master)](https://travis-ci.org/dryewo/cyrus)
[![Clojars Project](https://img.shields.io/clojars/v/cyrus/lein-template.svg)](https://clojars.org/cyrus/lein-template)

Includes:

* [mount-lite] for state management
* dev/user.clj for REPL-driven development
* [timbre]+[dovetail] for logging
* +http: [aleph] + [Compojure] (and [Ring])
* +db: PostgreSQL, [conman], [migratus], [HugSQL]
* +[nrepl]: NREPL server for remote debugging
* +[swagger1st] (RESTful API), includes +http
* Lean configuration management with [squeeze]
* useful tweaks

Roadmap:

* [Hystrix]
* Dockerfile

## Usage

```
$ lein new cyrus org.example.footeam/bar-project +http +db +nrepl
```

Read below for the list of available options.

Additionally, you can use `+all` option that includes everything.

## Contents

### State management and dependency injection

[mount-lite] is chosen over [component] for its lean spirit and ease of use. While in component you have to explicitly
define dependencies between parts of the system, mount-lite (and mount) figure this out by scanning namespace declarations.
This is a great example of DRY (don't repeat yourself) principle.

In mount-lite all states (components) have global names (like normal vars created with `def` or `defn`) and
are *dereferable* (implementing `IDeref`) — that means, you have to explicitly `@` or `deref` to access them.

[mount-lite] is chosen over [mount], because:
* mount supports dereferable states via `(in-cljc-mode)`, but when you try to `deref` a stopped state, mount automatically starts it.
  This leads to surprises and sometimes to impossibility to stop a running system (when you have a background process that
  accesses some state even after you execute `(m/stop)`. The state just starts again, including the states it depends on).  
  mount-lite in this case just throws an exception.
* In mount it is only possible to start a part of the system (for testing it) by explicitly listing
  states that you need. In mount-lite there is "start-up-to" functionality that automatically figures out
  which states does this specific state need and starts only them (see examples in tests in a generated project).

### Logging

Logging is supported via [timbre].  
Additionally, [dovetail] helper library provides useful tweaks:
* Log level per namespace is configured in core.clj using `log/set-ns-log-levels!`. The resulting log level 
per namespace is the higher of global and specific settings.
* All logging functions support throwable as their first argument.
* All formatted values (`%s`) are automatically `pr-str`ed. 
* Overall log level can be overridden by setting `LOG_LEVEL` environment variable.

### HTTP server

`+http` option adds a component that starts an HTTP server ([aleph]).
Example routes are provided as well as reasonable default middleware.

### Swagger1st API

`+swagger1st` adds `/api` route that is handled by a separate library: [Swagger1st].  
It allows request routing and parameter parsing based on [OpenAPI] definition in YAML format.
Highly recommended for any more or less serious API service.

Additionally exposes Swagger UI and spec:
* `/api/ui`
* `/api/swagger.json` 

This option automatically includes `+http`.

A variant of this option `+swagger1st-oauth2` includes OAuth2 protection of API endpoints, checking
access tokens against Introspection Endpoint configured by `TOKENINFO_URL` environment variable.
See [fahrscheine-bitte] for more information.

### DB access

`+db` option adds a component that includes database access layer, built on:

* [HugSQL] to generate access functions. Additionally, some interceptors are provided
  to convert `camel_case` column names to `:kebab-case` keywords in result maps.
* [conman] for connection pool.
* [migratus] for schema migrations.
  Migrations are always applied when the DB component starts up.
* PostgreSQL JDBC driver is included.
* Example schema is generated and example unit tests are provided.

PostgreSQL 9.6 for development and testing can be launched in a Docker container:

```sh
./make.sh db
```

### NREPL

`+nrepl` adds a NREPL server that is started before the main application is. It has to be enabled
by setting `NREPL_ENABLED=true`. Default port is `55000`, can be changed by setting `NREPL_PORT`. 

### Configuration management

The app can only be configured through environment variables (following [Twelve-Factor App manifesto]).
Each component defines its own schema ([prismatic/schema]), which is used to 
validate and coerce (using [squeeze]) the selected environment variables into it during component start-up:

Having a schema like this:
```clj
(s/defschema Config
  {(s/optional-key :http-port)            s/Int
   (s/optional-key :http-whitelisted-ips) [s/Str]})
```

Only mentioned environment variables are selected and transformed according to the schema:

```
HTTP_PORT=7777
HTTP_WHITELISTED_IPS="[1.2.3.4, 4.3.2.1]"
HTTP_UNKNOWN=nfrjbqogpq-u8y7894yr984gbl
```

yields:

```clj
{:http-port            7777                    ; parsed as Int
 :http-whitelisted-ips ["1.2.3.4" "4.3.2.1"]   ; parsed as YAML
```

The environment is read from [environ] on the component start and overridden
by args given to `(mount/start-with-args)` to allow experimenting with configuration without restarting REPL every time.

### user.clj

To facilitate REPL-driven development, `user.clj` contains functions, available directly from `user` namespace after REPL is started:

* `(start)`, which calls `(m/start)`, first re-reading environment variable overrides from `dev-env.edn`.  
  This is done to enable adjusting environment variables without restarting REPL.
* `(stop)`, which calls `(m/stop)`
* `(reset)`, which calls `(stop)`, `(refresh)` and then `(start)`
* `(tests)`, which runs all tests in all test namespaces
* functions from `clojure.tools.namespace`:
    * wrapped `(refresh)`, which stops started states before reloading and then starts them again
    * `(refresh-all)`, which reloads all the code in the project

## Development

In order to try the template out without releasing to clojars, install it to the local `~/.m2` and specify `--snapshot` flag:

```
$ lein install
$ cd target    # Or any other directory
$ lein new cyrus org.example.footeam/bar-project --snapshot -- +swagger1st +nrepl +db +http
```

## Testing

```
$ ./itest.sh
```

## FAQ

**Q.** Why another template? There is already [Luminus], which is more feature-rich.  
**A.** While Luminus is a great project, made with a lot of love, it already carries an opinion, which in some points is 
 different from mine. Also, even without additional options it generates too much (not always you need front-end, configuration 
 management with cprop is IMHO an overkill etc.)  
 Leiningen templates are not extensible, so I had to make my own, reusing best parts of Luminus.

## License

Copyright © 2017 Dmitrii Balakhonskii

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[squeeze]: https://github.com/dryewo/squeeze
[mount]: https://github.com/tolitius/mount
[mount-lite]: https://github.com/aroemers/mount-lite
[timbre]: https://github.com/ptaoussanis/timbre
[dovetail]: https://github.com/dryewo/dovetail
[aleph]: https://github.com/ztellman/aleph
[Compojure]: https://github.com/weavejester/compojure
[Ring]: https://github.com/ring-clojure/ring
[conman]: https://github.com/luminus-framework/conman
[migratus]: https://github.com/yogthos/migratus
[HugSQL]: https://www.hugsql.org/
[nrepl]: https://github.com/clojure/tools.nrepl
[swagger1st]: https://github.com/zalando-stups/swagger1st
[Hystrix]: https://github.com/Netflix/Hystrix/tree/master/hystrix-contrib/hystrix-clj
[component]: https://github.com/stuartsierra/component
[OpenAPI]: https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md
[Twelve-Factor App manifesto]: https://12factor.net/config
[prismatic/schema]: https://github.com/plumatic/schema
[environ]: https://github.com/weavejester/environ
[Luminus]: https://github.com/luminus-framework/luminus-template
[fahrscheine-bitte]: https://github.com/dryewo/fahrscheine-bitte
