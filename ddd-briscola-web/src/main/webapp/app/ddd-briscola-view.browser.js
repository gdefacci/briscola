/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId])
/******/ 			return installedModules[moduleId].exports;
/******/
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			exports: {},
/******/ 			id: moduleId,
/******/ 			loaded: false
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.loaded = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(0);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __assign = (this && this.__assign) || Object.assign || function(t) {
	    for (var s, i = 1, n = arguments.length; i < n; i++) {
	        s = arguments[i];
	        for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
	            t[p] = s[p];
	    }
	    return t;
	};
	var ddd_briscola_app_1 = __webpack_require__(1);
	var listeners_1 = __webpack_require__(40);
	var Board_1 = __webpack_require__(41);
	__webpack_require__(50);
	var app;
	var reactContainer = "react-container";
	function main() {
	    app = ddd_briscola_app_1.App.create("site-map");
	    var el = document.getElementById(reactContainer);
	    if (el !== null) {
	        app.displayChannel.subscribe(function (board) {
	            var boardProps = listeners_1.createBoardCommandListener(board, function (cmd) { return app.exec(cmd); });
	            console.log("boardProps ");
	            console.log(boardProps);
	            ReactDOM.render(React.createElement(Board_1.Board, __assign({}, boardProps)), el);
	        });
	        app.exec(new ddd_briscola_app_1.Commands.StarApplication());
	    }
	    else {
	        alert("cant find element " + reactContainer);
	    }
	}
	exports.main = main;
	window.addEventListener("load", function (event) { return main(); });


/***/ },
/* 1 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	function __export(m) {
	    for (var p in m) if (!exports.hasOwnProperty(p)) exports[p] = m[p];
	}
	__export(__webpack_require__(2));
	var Commands = __webpack_require__(39);
	exports.Commands = Commands;


/***/ },
/* 2 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var CommandsDispatcher_1 = __webpack_require__(3);
	var ApplicationState_1 = __webpack_require__(4);
	var ApplicationDispatch_1 = __webpack_require__(37);
	var App;
	(function (App) {
	    function create(entryPoint) {
	        return new AppImpl(ApplicationState_1.initialState(entryPoint), ApplicationDispatch_1.default());
	    }
	    App.create = create;
	})(App = exports.App || (exports.App = {}));
	var AppImpl = (function () {
	    function AppImpl(initState, reducerFunction) {
	        var _this = this;
	        this.changesChannel = new Rx.ReplaySubject();
	        this.dispatcher = initState.then(function (state) {
	            return CommandsDispatcher_1.default(state, reducerFunction, function (s) { return _this.changesChannel.onNext(s); });
	        });
	        this.displayChannel = Rx.Observable.fromPromise(this.dispatcher).flatMap(function (d) { return _this.changesChannel.map(function (s) { return s.board; }); });
	        this.displayChannel.subscribe(function (ev) {
	            console.log("display channel ");
	            console.log(ev);
	            console.log("***************");
	        }, function (err) {
	            console.error(err);
	        });
	    }
	    AppImpl.prototype.exec = function (cmd) {
	        this.dispatcher.then(function (dispatch) {
	            dispatch(cmd).catch(function (err) { return console.error(err); });
	        });
	    };
	    return AppImpl;
	}());


/***/ },
/* 3 */
/***/ function(module, exports) {

	"use strict";
	function commandDispatcher(initialState, dispatchFuntion, effect) {
	    var currentState = initialState;
	    var dispatch = function (cmd) {
	        var reducer = dispatchFuntion(cmd);
	        return reducer(currentState, dispatch).then(function (newState) {
	            currentState = newState;
	            effect(newState);
	            return newState;
	        });
	    };
	    return dispatch;
	}
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = commandDispatcher;


/***/ },
/* 4 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var PlayersService_1 = __webpack_require__(14);
	var PlayerService_1 = __webpack_require__(36);
	var nrest_fetch_1 = __webpack_require__(19);
	var ddd_briscola_model_1 = __webpack_require__(15);
	var Util_1 = __webpack_require__(35);
	function initialState(entryPoint) {
	    var reqFactory = Util_1.Http.createRequestFactory({
	        method: "GET"
	    });
	    var resourceFetch = new nrest_fetch_1.ResourceFetch(nrest_fetch_1.ExtraPropertiesStrategy.copy, nrest_fetch_1.httpCacheFactory(reqFactory, Util_1.Http.jsonResponseReader));
	    var createPlayerService = function (player) { return new PlayerService_1.PlayerService(resourceFetch, player); };
	    return resourceFetch.fetchResource(entryPoint, nrest_fetch_1.mapping(ddd_briscola_model_1.SiteMap)).then(function (siteMap) {
	        return {
	            playersService: new PlayersService_1.PlayersService(resourceFetch, siteMap),
	            playerService: flib_1.Option.None,
	            board: ddd_briscola_model_1.Board.empty(),
	            createPlayerService: createPlayerService
	        };
	    });
	}
	exports.initialState = initialState;


/***/ },
/* 5 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var Arrays_1 = __webpack_require__(6);
	exports.Arrays = Arrays_1.default;
	var JsMap_1 = __webpack_require__(9);
	exports.JsMap = JsMap_1.default;
	var Option_1 = __webpack_require__(7);
	exports.Option = Option_1.default;
	var lazy_1 = __webpack_require__(10);
	exports.lazy = lazy_1.default;
	var fail_1 = __webpack_require__(11);
	exports.fail = fail_1.default;
	var Either_1 = __webpack_require__(12);
	exports.Either = Either_1.default;
	var Try_1 = __webpack_require__(13);
	exports.Try = Try_1.default;
	var isNull_1 = __webpack_require__(8);
	exports.isNull = isNull_1.default;


/***/ },
/* 6 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var Option_1 = __webpack_require__(7);
	var isNull_1 = __webpack_require__(8);
	var Arrays;
	(function (Arrays) {
	    function flatMap(arr, f) {
	        var res = [];
	        arr.forEach(function (a) { return res = res.concat(f(a)); });
	        return res;
	    }
	    Arrays.flatMap = flatMap;
	    function find(arr, pred) {
	        var opt = findIndex(arr, pred);
	        if (opt.isDefined())
	            return opt.map(function (idx) { return arr[idx]; });
	        else
	            return Option_1.default.None;
	    }
	    Arrays.find = find;
	    function findIndex(arr, pred) {
	        for (var i = 0; i < arr.length; i++) {
	            var v = arr[i];
	            if (pred(v))
	                return Option_1.default.some(i);
	        }
	        return Option_1.default.None;
	    }
	    Arrays.findIndex = findIndex;
	    function split(arr, pred) {
	        var res1 = [];
	        var res2 = [];
	        arr.forEach(function (i) {
	            if (pred(i))
	                res1.push(i);
	            else
	                res2.push(i);
	        });
	        return [res1, res2];
	    }
	    Arrays.split = split;
	    function exists(arr, pred) {
	        for (var i = 0; i < arr.length; i++) {
	            var v = arr[i];
	            if (pred(v))
	                return true;
	        }
	        return false;
	    }
	    Arrays.exists = exists;
	    function foldLeft(arr, z, f) {
	        var res = z;
	        arr.forEach(function (i) { return res = f(res, i); });
	        return res;
	    }
	    Arrays.foldLeft = foldLeft;
	    function foldRight(arr, z, f) {
	        var res = z;
	        var len = arr.length;
	        for (var idx = len - 1; idx >= 0; idx--) {
	            res = f(arr[idx], res);
	        }
	        return res;
	    }
	    Arrays.foldRight = foldRight;
	    function groupBy(arr, group) {
	        var res = {};
	        var addToMap = function (k, v) {
	            var arr = res[k];
	            if (isNull_1.default(arr)) {
	                res[k] = [v];
	            }
	            else {
	                arr.push(v);
	            }
	        };
	        arr.forEach(function (e) { return addToMap(group(e), e); });
	        return res;
	    }
	    Arrays.groupBy = groupBy;
	})(Arrays || (Arrays = {}));
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = Arrays;


/***/ },
/* 7 */
/***/ function(module, exports) {

	"use strict";
	var Option = (function () {
	    function Option(value) {
	        this.value = value;
	    }
	    Option.some = function (t) {
	        return new Option(t);
	    };
	    Option.option = function (v) {
	        return new Option(v);
	    };
	    Option.prototype.isEmpty = function () {
	        return this.value === null || this.value === undefined;
	    };
	    Option.prototype.isDefined = function () {
	        return !this.isEmpty();
	    };
	    Option.prototype.fold = function (fnone, fsome) {
	        return (this.value === null || this.value === undefined) ? fnone() : fsome(this.value);
	    };
	    Option.prototype.forEach = function (f) {
	        if (!(this.value === null || this.value === undefined))
	            f(this.value);
	    };
	    Option.prototype.map = function (f) {
	        return this.fold(function () { return Option.None; }, function (v) { return new Option(f(v)); });
	    };
	    Option.prototype.flatMap = function (f) {
	        return this.fold(function () { return Option.None; }, function (v) { return f(v); });
	    };
	    Option.prototype.getOrElse = function (f) {
	        return this.fold(function () { return f(); }, function (v) { return v; });
	    };
	    Option.prototype.orElse = function (f) {
	        return this.fold(f, function (v) { return new Option(v); });
	    };
	    Option.prototype.zip = function (b) {
	        if (this.value !== null && this.value !== undefined) {
	            var v_1 = this.value;
	            return b.map(function (b1) {
	                var r = [v_1, b1];
	                return r;
	            });
	        }
	        else {
	            return Option.None;
	        }
	    };
	    Option.prototype.toArray = function () {
	        return this.fold(function () { return []; }, function (v) { return [v]; });
	    };
	    Option.prototype.toString = function () {
	        return this.fold(function () { return "None"; }, function (v) { return ("Some(" + v + ")"); });
	    };
	    Option.None = new Option(null);
	    return Option;
	}());
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = Option;


/***/ },
/* 8 */
/***/ function(module, exports) {

	"use strict";
	function default_1(a) {
	    return a === undefined || a === null;
	}
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = default_1;


/***/ },
/* 9 */
/***/ function(module, exports) {

	"use strict";
	var JsMap;
	(function (JsMap) {
	    function create(kps) {
	        var res = {};
	        kps.forEach(function (kp) { return res[kp.key] = kp.value; });
	        return res;
	    }
	    JsMap.create = create;
	    function forEach(mp, f) {
	        Object.keys(mp).forEach(function (k) { return f(k, mp[k]); });
	    }
	    JsMap.forEach = forEach;
	    function map(mp, f) {
	        var res = {};
	        JsMap.forEach(mp, function (k, v) { return res[k] = f(k, v); });
	        return res;
	    }
	    JsMap.map = map;
	    function merge(mps) {
	        var res = {};
	        mps.forEach(function (mp) { return JsMap.forEach(mp, function (k, v) { return res[k] = v; }); });
	        return res;
	    }
	    JsMap.merge = merge;
	    function flatMap(mp, f) {
	        var res = {};
	        JsMap.forEach(mp, function (k, v) { return JsMap.forEach(f(k, v), function (k, v) { return res[k] = v; }); });
	        return res;
	    }
	    JsMap.flatMap = flatMap;
	})(JsMap || (JsMap = {}));
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = JsMap;


/***/ },
/* 10 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var Option_1 = __webpack_require__(7);
	function lazy(f) {
	    var value = Option_1.default.None;
	    return function () {
	        return value.fold(function () {
	            var v = f();
	            value = Option_1.default.some(v);
	            return v;
	        }, function (v) { return v; });
	    };
	}
	/*not used */
	var Lazy = (function () {
	    function Lazy(f) {
	        this.value = lazy(f);
	    }
	    Lazy.prototype.map = function (f) {
	        var _this = this;
	        return new Lazy(function () { return f(_this.value()); });
	    };
	    Lazy.prototype.flatMap = function (f) {
	        var _this = this;
	        return new Lazy(function () { return f(_this.value()).value(); });
	    };
	    return Lazy;
	}());
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = lazy;


/***/ },
/* 11 */
/***/ function(module, exports) {

	"use strict";
	function fail(msg) {
	    throw new Error(msg);
	}
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = fail;


/***/ },
/* 12 */
/***/ function(module, exports) {

	"use strict";
	function isLeft(v) {
	    return v[0] != null;
	}
	var Either = (function () {
	    function Either(value) {
	        this.value = value;
	    }
	    Either.right = function (t) {
	        return new Either([null, t]);
	    };
	    Either.left = function (l) {
	        return new Either([l, null]);
	    };
	    Either.prototype.fold = function (onError, onSuccess) {
	        if (isLeft(this.value))
	            return onError(this.value[0]);
	        else
	            return onSuccess(this.value[1]);
	    };
	    Either.prototype.isLeft = function () { return this.fold(function () { return true; }, function () { return false; }); };
	    Either.prototype.isRight = function () { return this.fold(function () { return true; }, function () { return false; }); };
	    Either.prototype.map = function (f) {
	        return this.fold(function (l) { return Either.left(l); }, function (r) { return Either.right(f(r)); });
	    };
	    Either.prototype.flatMap = function (f) {
	        return this.fold(function (err) { return Either.left(err); }, function (v) { return f(v); });
	    };
	    Either.prototype.leftMap = function (f) {
	        return this.fold(function (l) { return Either.left(f(l)); }, function (r) { return Either.right(r); });
	    };
	    return Either;
	}());
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = Either;


/***/ },
/* 13 */
/***/ function(module, exports) {

	"use strict";
	function isLeft(v) {
	    return v[0] != null;
	}
	var Try = (function () {
	    function Try(value) {
	        this.value = value;
	    }
	    Try.fromValue = function (v) {
	        try {
	            return Try.success(v());
	        }
	        catch (e) {
	            return Try.failure(e);
	        }
	    };
	    Try.success = function (t) {
	        return new Try([null, t]);
	    };
	    Try.failure = function (err) {
	        return new Try([err, null]);
	    };
	    Try.prototype.fold = function (onError, onSuccess) {
	        if (isLeft(this.value))
	            return onError(this.value[0]);
	        else
	            return onSuccess(this.value[1]);
	    };
	    Try.prototype.isEmpty = function () { return this.fold(function () { return true; }, function () { return false; }); };
	    Try.prototype.isDefined = function () { return this.fold(function () { return true; }, function () { return false; }); };
	    Try.prototype.map = function (f) {
	        return this.fold(function (err) { return Try.failure(err); }, function (v) { return Try.fromValue(function () { return f(v); }); });
	    };
	    Try.prototype.flatMap = function (f) {
	        try {
	            return this.fold(function (err) { return Try.failure(err); }, function (v) { return f(v); });
	        }
	        catch (e) {
	            return Try.failure(e);
	        }
	    };
	    Try.prototype.toPromise = function () {
	        return this.fold(function (err) { return Promise.reject(err); }, function (v) { return Promise.resolve(v); });
	    };
	    return Try;
	}());
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = Try;


/***/ },
/* 14 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var ddd_briscola_model_1 = __webpack_require__(15);
	var nrest_fetch_1 = __webpack_require__(19);
	var Util_1 = __webpack_require__(35);
	var PlayersService = (function () {
	    function PlayersService(resourceFetch, siteMap) {
	        this.resourceFetch = resourceFetch;
	        this.siteMap = siteMap;
	    }
	    PlayersService.prototype.allPlayers = function () {
	        return this.resourceFetch.fetchResource(this.siteMap.players, nrest_fetch_1.mapping(ddd_briscola_model_1.PlayersCollection)).then(function (c) { return c.members; });
	    };
	    PlayersService.prototype.player = function (playerSelf) {
	        return this.resourceFetch.fetchResource(playerSelf, nrest_fetch_1.mapping(ddd_briscola_model_1.Player));
	    };
	    PlayersService.prototype.createPlayer = function (name, password) {
	        var _this = this;
	        return Util_1.Http.POST(this.siteMap.players, {
	            name: name,
	            password: password
	        }).then(function (resp) {
	            return resp.json().then(function (pl) { return _this.resourceFetch.fetchObject(pl, nrest_fetch_1.mapping(ddd_briscola_model_1.CurrentPlayer)); });
	        });
	    };
	    PlayersService.prototype.logon = function (name, password) {
	        var _this = this;
	        return Util_1.Http.POST(this.siteMap.playerLogin, {
	            name: name,
	            password: password
	        }).then(function (resp) { return resp.json().then(function (pl) { return _this.resourceFetch.fetchObject(pl, nrest_fetch_1.mapping(ddd_briscola_model_1.CurrentPlayer)); }); });
	    };
	    return PlayersService;
	}());
	exports.PlayersService = PlayersService;


/***/ },
/* 15 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	function __export(m) {
	    for (var p in m) if (!exports.hasOwnProperty(p)) exports[p] = m[p];
	}
	__export(__webpack_require__(16));
	__export(__webpack_require__(17));
	__export(__webpack_require__(29));
	__export(__webpack_require__(30));
	__export(__webpack_require__(31));
	__export(__webpack_require__(18));
	__export(__webpack_require__(32));
	__export(__webpack_require__(33));
	var Input = __webpack_require__(34);
	exports.Input = Input;


/***/ },
/* 16 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var Board;
	(function (Board) {
	    function empty() {
	        return {
	            player: flib_1.Option.None,
	            players: [],
	            currentGame: flib_1.Option.None,
	            activeGames: {},
	            finishedGames: {},
	            engagedCompetitions: {},
	            competitionSelectedPlayers: {},
	            competitionKind: "single-match",
	            competitionDeadlineKind: "all-players",
	            eventsLog: [],
	            viewFlag: ViewFlag.normal,
	            config: {
	                minPlayersNumber: 2,
	                maxPlayersNumber: 8
	            }
	        };
	    }
	    Board.empty = empty;
	})(Board = exports.Board || (exports.Board = {}));
	(function (ViewFlag) {
	    ViewFlag[ViewFlag["normal"] = 0] = "normal";
	    ViewFlag[ViewFlag["showPlayerCards"] = 1] = "showPlayerCards";
	})(exports.ViewFlag || (exports.ViewFlag = {}));
	var ViewFlag = exports.ViewFlag;


/***/ },
/* 17 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
	    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
	    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
	    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
	    return c > 3 && r && Object.defineProperty(target, key, r), r;
	};
	var __metadata = (this && this.__metadata) || function (k, v) {
	    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
	};
	var flib_1 = __webpack_require__(5);
	var player_1 = __webpack_require__(18);
	var nrest_fetch_1 = __webpack_require__(19);
	var Util_1 = __webpack_require__(28);
	(function (MatchKindKind) {
	    MatchKindKind[MatchKindKind["singleMatch"] = 0] = "singleMatch";
	    MatchKindKind[MatchKindKind["numberOfGamesMatchKind"] = 1] = "numberOfGamesMatchKind";
	    MatchKindKind[MatchKindKind["targetPointsMatchKind"] = 2] = "targetPointsMatchKind";
	})(exports.MatchKindKind || (exports.MatchKindKind = {}));
	var MatchKindKind = exports.MatchKindKind;
	(function (CompetitionStateKind) {
	    CompetitionStateKind[CompetitionStateKind["open"] = 0] = "open";
	    CompetitionStateKind[CompetitionStateKind["dropped"] = 1] = "dropped";
	    CompetitionStateKind[CompetitionStateKind["fullfilled"] = 2] = "fullfilled";
	})(exports.CompetitionStateKind || (exports.CompetitionStateKind = {}));
	var CompetitionStateKind = exports.CompetitionStateKind;
	(function (CompetitionStartDeadlineKind) {
	    CompetitionStartDeadlineKind[CompetitionStartDeadlineKind["allPlayers"] = 0] = "allPlayers";
	    CompetitionStartDeadlineKind[CompetitionStartDeadlineKind["onPlayerCount"] = 1] = "onPlayerCount";
	})(exports.CompetitionStartDeadlineKind || (exports.CompetitionStartDeadlineKind = {}));
	var CompetitionStartDeadlineKind = exports.CompetitionStartDeadlineKind;
	exports.toCompetitionStartDeadline = nrest_fetch_1.Lazy.choose("CompetitionStartDeadline", [
	    function (a) { return a.kind === CompetitionStartDeadlineKind[CompetitionStartDeadlineKind.allPlayers]; },
	    function () { return AllPlayers; }
	], [
	    function (a) { return a.kind === CompetitionStartDeadlineKind[CompetitionStartDeadlineKind.onPlayerCount]; },
	    function () { return OnPlayerCount; }
	]);
	var AllPlayers = (function () {
	    function AllPlayers() {
	    }
	    Object.defineProperty(AllPlayers.prototype, "kind", {
	        get: function () {
	            return CompetitionStartDeadlineKind.allPlayers;
	        },
	        enumerable: true,
	        configurable: true
	    });
	    return AllPlayers;
	}());
	exports.AllPlayers = AllPlayers;
	var OnPlayerCount = (function () {
	    function OnPlayerCount() {
	    }
	    Object.defineProperty(OnPlayerCount.prototype, "kind", {
	        get: function () {
	            return CompetitionStartDeadlineKind.onPlayerCount;
	        },
	        enumerable: true,
	        configurable: true
	    });
	    return OnPlayerCount;
	}());
	exports.OnPlayerCount = OnPlayerCount;
	/*
	export function toMatchKind(kind:string):MatchKind {
	  switch(kind) {
	    case MatchKindKind[MatchKindKind.singleMatch] : return new SingleMatch
	    case MatchKindKind[MatchKindKind.numberOfGamesMatchKind] :return new NumberOfGamesMatchKind
	    case MatchKindKind[MatchKindKind.targetPointsMatchKind] :return new TargetPointsMatchKind
	    default: throw new Error("invalid match kind "+kind)
	  }
	}
	*/
	exports.matchKindChoice = nrest_fetch_1.Lazy.choose("MatchKind", [
	    function (a) { return a.kind === MatchKindKind[MatchKindKind.singleMatch]; },
	    function () { return SingleMatch; }
	], [
	    function (a) { return a.kind === MatchKindKind[MatchKindKind.targetPointsMatchKind]; },
	    function () { return TargetPointsMatchKind; }
	]);
	var SingleMatch = (function () {
	    function SingleMatch() {
	    }
	    Object.defineProperty(SingleMatch.prototype, "kind", {
	        get: function () {
	            return MatchKindKind.singleMatch;
	        },
	        enumerable: true,
	        configurable: true
	    });
	    return SingleMatch;
	}());
	exports.SingleMatch = SingleMatch;
	var NumberOfGamesMatchKind = (function () {
	    function NumberOfGamesMatchKind() {
	    }
	    Object.defineProperty(NumberOfGamesMatchKind.prototype, "kind", {
	        get: function () {
	            return MatchKindKind.numberOfGamesMatchKind;
	        },
	        enumerable: true,
	        configurable: true
	    });
	    return NumberOfGamesMatchKind;
	}());
	exports.NumberOfGamesMatchKind = NumberOfGamesMatchKind;
	var TargetPointsMatchKind = (function () {
	    function TargetPointsMatchKind() {
	    }
	    Object.defineProperty(TargetPointsMatchKind.prototype, "kind", {
	        get: function () {
	            return MatchKindKind.numberOfGamesMatchKind;
	        },
	        enumerable: true,
	        configurable: true
	    });
	    return TargetPointsMatchKind;
	}());
	exports.TargetPointsMatchKind = TargetPointsMatchKind;
	var Competition = (function () {
	    function Competition() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOfLinks(player_1.Player)), 
	        __metadata('design:type', Array)
	    ], Competition.prototype, "players", void 0);
	    __decorate([
	        nrest_fetch_1.convert(exports.matchKindChoice), 
	        __metadata('design:type', Object)
	    ], Competition.prototype, "kind", void 0);
	    __decorate([
	        nrest_fetch_1.convert(exports.toCompetitionStartDeadline), 
	        __metadata('design:type', Object)
	    ], Competition.prototype, "deadline", void 0);
	    return Competition;
	}());
	exports.Competition = Competition;
	var stringToCompetitionStateKind = nrest_fetch_1.Value.string().map(Util_1.toEnum(CompetitionStateKind, "CompetitionStateKind"));
	var CompetitionState = (function () {
	    function CompetitionState() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(function () { return stringToCompetitionStateKind; }), 
	        __metadata('design:type', Number)
	    ], CompetitionState.prototype, "kind", void 0);
	    __decorate([
	        nrest_fetch_1.convert(), 
	        __metadata('design:type', Competition)
	    ], CompetitionState.prototype, "competition", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOfLinks(player_1.Player)), 
	        __metadata('design:type', Array)
	    ], CompetitionState.prototype, "acceptingPlayers", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOfLinks(player_1.Player)), 
	        __metadata('design:type', Array)
	    ], CompetitionState.prototype, "decliningPlayers", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.Value.option(nrest_fetch_1.Value.string)), 
	        __metadata('design:type', flib_1.Option)
	    ], CompetitionState.prototype, "accept", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.Value.option(nrest_fetch_1.Value.string)), 
	        __metadata('design:type', flib_1.Option)
	    ], CompetitionState.prototype, "decline", void 0);
	    return CompetitionState;
	}());
	exports.CompetitionState = CompetitionState;
	exports.competitionStateChoice = nrest_fetch_1.Lazy.choose("CompetitionState", [
	    function (a) { return a.kind === CompetitionStateKind[CompetitionStateKind.dropped]; },
	    function () { return CompetitionState; }
	], [
	    function (a) { return a.kind === CompetitionStateKind[CompetitionStateKind.fullfilled]; },
	    function () { return CompetitionState; }
	], [
	    function (a) { return a.kind === CompetitionStateKind[CompetitionStateKind.open]; },
	    function () { return CompetitionState; }
	]);


/***/ },
/* 18 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
	    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
	    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
	    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
	    return c > 3 && r && Object.defineProperty(target, key, r), r;
	};
	var __metadata = (this && this.__metadata) || function (k, v) {
	    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
	};
	var nrest_fetch_1 = __webpack_require__(19);
	var Player = (function () {
	    function Player() {
	    }
	    return Player;
	}());
	exports.Player = Player;
	var CurrentPlayer = (function (_super) {
	    __extends(CurrentPlayer, _super);
	    function CurrentPlayer() {
	        _super.apply(this, arguments);
	    }
	    return CurrentPlayer;
	}(Player));
	exports.CurrentPlayer = CurrentPlayer;
	(function (PlayerEventKind) {
	    PlayerEventKind[PlayerEventKind["playerLogOn"] = 0] = "playerLogOn";
	    PlayerEventKind[PlayerEventKind["playerLogOff"] = 1] = "playerLogOff";
	})(exports.PlayerEventKind || (exports.PlayerEventKind = {}));
	var PlayerEventKind = exports.PlayerEventKind;
	var Team = (function () {
	    function Team() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOfLinks(Player)), 
	        __metadata('design:type', Array)
	    ], Team.prototype, "players", void 0);
	    return Team;
	}());
	exports.Team = Team;
	var PlayerLogOn = (function () {
	    function PlayerLogOn() {
	    }
	    Object.defineProperty(PlayerLogOn.prototype, "eventName", {
	        get: function () {
	            return PlayerEventKind[PlayerEventKind.playerLogOn];
	        },
	        enumerable: true,
	        configurable: true
	    });
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', Player)
	    ], PlayerLogOn.prototype, "player", void 0);
	    return PlayerLogOn;
	}());
	exports.PlayerLogOn = PlayerLogOn;
	var PlayerLogOff = (function () {
	    function PlayerLogOff() {
	    }
	    Object.defineProperty(PlayerLogOff.prototype, "eventName", {
	        get: function () {
	            return PlayerEventKind[PlayerEventKind.playerLogOff];
	        },
	        enumerable: true,
	        configurable: true
	    });
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', Player)
	    ], PlayerLogOff.prototype, "player", void 0);
	    return PlayerLogOff;
	}());
	exports.PlayerLogOff = PlayerLogOff;
	exports.playerEvents = [
	    [
	        function (a) { return a.kind === PlayerEventKind[PlayerEventKind.playerLogOn]; },
	        function () { return PlayerLogOn; }
	    ], [
	        function (a) { return a.kind === PlayerEventKind[PlayerEventKind.playerLogOff]; },
	        function () { return PlayerLogOff; }
	    ]
	];
	exports.playerEventChoice = nrest_fetch_1.Lazy.choose.apply(nrest_fetch_1.Lazy, ["PlayerEvent"].concat(exports.playerEvents));
	var PlayersCollection = (function () {
	    function PlayersCollection() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOfLinks(Player)), 
	        __metadata('design:type', Array)
	    ], PlayersCollection.prototype, "members", void 0);
	    return PlayersCollection;
	}());
	exports.PlayersCollection = PlayersCollection;


/***/ },
/* 19 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	function __export(m) {
	    for (var p in m) if (!exports.hasOwnProperty(p)) exports[p] = m[p];
	}
	__export(__webpack_require__(20));
	__export(__webpack_require__(21));
	__export(__webpack_require__(25));
	var Mappings_1 = __webpack_require__(22);
	exports.arrayOf = Mappings_1.arrayOf;
	exports.arrayOfLinks = Mappings_1.arrayOfLinks;
	exports.choose = Mappings_1.choose;
	exports.Lazy = Mappings_1.Lazy;
	exports.mapping = Mappings_1.mapping;
	exports.optionalLink = Mappings_1.optionalLink;
	exports.optionOf = Mappings_1.optionOf;
	var httpCacheFactory_1 = __webpack_require__(26);
	exports.httpCacheFactory = httpCacheFactory_1.default;
	var TestFetcher_1 = __webpack_require__(27);
	exports.TestFetcher = TestFetcher_1.default;


/***/ },
/* 20 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var flib_1 = __webpack_require__(5);
	(function (SimpleValueKind) {
	    SimpleValueKind[SimpleValueKind["booleanKind"] = 0] = "booleanKind";
	    SimpleValueKind[SimpleValueKind["numberKind"] = 1] = "numberKind";
	    SimpleValueKind[SimpleValueKind["textKind"] = 2] = "textKind";
	    SimpleValueKind[SimpleValueKind["getUrlKind"] = 3] = "getUrlKind";
	})(exports.SimpleValueKind || (exports.SimpleValueKind = {}));
	var SimpleValueKind = exports.SimpleValueKind;
	var Value = (function () {
	    function Value() {
	    }
	    return Value;
	}());
	exports.Value = Value;
	function foldCase(onRawValue, onLink, onObject, onOption, onSeq, onChoice) {
	    return function (v) {
	        if (v instanceof SimpleValue) {
	            return onRawValue(v.rawValue);
	        }
	        else if (v instanceof LinkValue) {
	            return onLink();
	        }
	        else if (v instanceof ObjectValue) {
	            return onObject();
	        }
	        else if (v instanceof OptionValue) {
	            return onOption();
	        }
	        else if (v instanceof ArrayValue) {
	            return onSeq();
	        }
	        else if (v instanceof ChoiceValue) {
	            return onChoice();
	        }
	        else {
	            throw new Error("invalid case ");
	        }
	    };
	}
	exports.foldCase = foldCase;
	var RawValue = (function () {
	    function RawValue(kind, f) {
	        this.kind = kind;
	        this.f = f;
	    }
	    RawValue.prototype.map = function (f1) {
	        var _this = this;
	        return new RawValue(this.kind, function (i) { return f1(_this.f(i)); });
	    };
	    RawValue.prototype.fold = function (onBoolean, onNumber, onString, onGetUrl) {
	        switch (this.kind) {
	            case SimpleValueKind.booleanKind: return onBoolean(this.f);
	            case SimpleValueKind.numberKind: return onNumber(this.f);
	            case SimpleValueKind.textKind: return onString(this.f);
	            case SimpleValueKind.getUrlKind: return onGetUrl(this.f);
	            default:
	                throw new Error("invalid match " + this.kind);
	        }
	    };
	    RawValue.boolean = new RawValue(SimpleValueKind.booleanKind, function (i) { return i; });
	    RawValue.number = new RawValue(SimpleValueKind.numberKind, function (i) { return i; });
	    RawValue.string = new RawValue(SimpleValueKind.textKind, function (i) { return i; });
	    RawValue.getUrl = new RawValue(SimpleValueKind.getUrlKind, function (i) { return i; });
	    return RawValue;
	}());
	exports.RawValue = RawValue;
	var SimpleValue = (function (_super) {
	    __extends(SimpleValue, _super);
	    function SimpleValue(rawValue) {
	        _super.call(this);
	        this.rawValue = rawValue;
	    }
	    SimpleValue.prototype.map = function (f1) {
	        return new SimpleValue(this.rawValue.map(f1));
	    };
	    SimpleValue.prototype.fold = function (onRawValue, onLink, onObject, onOption, onSeq, onChoice) {
	        return onRawValue(this.rawValue);
	    };
	    return SimpleValue;
	}(Value));
	exports.SimpleValue = SimpleValue;
	var OptionValue = (function (_super) {
	    __extends(OptionValue, _super);
	    function OptionValue(item) {
	        _super.call(this);
	        this.item = item;
	    }
	    OptionValue.prototype.fold = function (onRawValue, onLink, onObject, onOption, onSeq, onChoice) {
	        return onOption(this.item());
	    };
	    return OptionValue;
	}(Value));
	exports.OptionValue = OptionValue;
	var ArrayValue = (function (_super) {
	    __extends(ArrayValue, _super);
	    function ArrayValue(item) {
	        _super.call(this);
	        this.item = item;
	    }
	    ArrayValue.prototype.fold = function (onRawValue, onLink, onObject, onOption, onSeq, onChoice) {
	        return onSeq(this.item());
	    };
	    return ArrayValue;
	}(Value));
	exports.ArrayValue = ArrayValue;
	var LinkValue = (function (_super) {
	    __extends(LinkValue, _super);
	    function LinkValue(item, notFoundHandler) {
	        _super.call(this);
	        this.item = item;
	        this.notFoundHandler = notFoundHandler;
	    }
	    LinkValue.prototype.fold = function (onRawValue, onLink, onObject, onOption, onSeq, onChoice) {
	        return onLink(this.item(), this.notFoundHandler);
	    };
	    return LinkValue;
	}(Value));
	exports.LinkValue = LinkValue;
	(function (ExtraPropertiesStrategy) {
	    ExtraPropertiesStrategy[ExtraPropertiesStrategy["discard"] = 0] = "discard";
	    ExtraPropertiesStrategy[ExtraPropertiesStrategy["copy"] = 1] = "copy";
	    ExtraPropertiesStrategy[ExtraPropertiesStrategy["fail"] = 2] = "fail";
	})(exports.ExtraPropertiesStrategy || (exports.ExtraPropertiesStrategy = {}));
	var ExtraPropertiesStrategy = exports.ExtraPropertiesStrategy;
	var ObjectValue = (function (_super) {
	    __extends(ObjectValue, _super);
	    function ObjectValue(jsConstructor, properties, extraPropertiesStrategy) {
	        _super.call(this);
	        this.jsConstructor = jsConstructor;
	        this.properties = properties;
	        this.extraPropertiesStrategy = extraPropertiesStrategy;
	    }
	    ObjectValue.prototype.fold = function (onRawValue, onLink, onObject, onOption, onSeq, onChoice) {
	        if (this.props === undefined || this.props === null) {
	            this.props = flib_1.JsMap.map(this.properties, function (k, v) {
	                var _a = Array.isArray(v) ? v : [k, v], sourceProperty = _a[0], lazyValue = _a[1];
	                var r = [sourceProperty, lazyValue()];
	                return r;
	            });
	        }
	        return onObject(this.jsConstructor, this.props, this.extraPropertiesStrategy);
	    };
	    return ObjectValue;
	}(Value));
	exports.ObjectValue = ObjectValue;
	var ChoiceValue = (function (_super) {
	    __extends(ChoiceValue, _super);
	    function ChoiceValue(description, items) {
	        _super.call(this);
	        this.description = description;
	        this.items = items;
	    }
	    ChoiceValue.prototype.fold = function (onRawValue, onLink, onObject, onOption, onSeq, onChoice) {
	        return onChoice(this.description, this.items);
	    };
	    return ChoiceValue;
	}(Value));
	exports.ChoiceValue = ChoiceValue;
	var ValuePredicate = (function () {
	    function ValuePredicate(predicate, value) {
	        this.predicate = predicate;
	        this.value = value;
	    }
	    return ValuePredicate;
	}());
	exports.ValuePredicate = ValuePredicate;
	var NotFoundHandler;
	(function (NotFoundHandler) {
	    function defaultTo(defaultValue) {
	        return flib_1.lazy(function () { return defaultValue; });
	    }
	    NotFoundHandler.defaultTo = defaultTo;
	    function raiseNotFoundError() {
	        return function (resourceName) { return flib_1.fail("not found :" + resourceName); };
	    }
	    NotFoundHandler.raiseNotFoundError = raiseNotFoundError;
	})(NotFoundHandler = exports.NotFoundHandler || (exports.NotFoundHandler = {}));
	var Value;
	(function (Value) {
	    function match(pred) {
	        return function (value) {
	            return new ValuePredicate(pred, value);
	        };
	    }
	    Value.match = match;
	    Value.boolean = flib_1.lazy(function () { return new SimpleValue(RawValue.boolean); });
	    Value.number = flib_1.lazy(function () { return new SimpleValue(RawValue.number); });
	    Value.string = flib_1.lazy(function () { return new SimpleValue(RawValue.string); });
	    Value.getUrl = flib_1.lazy(function () { return new SimpleValue(RawValue.getUrl.map(function (v) { return v.getOrElse(function () { return flib_1.fail("missing parent url"); }); })); });
	    function option(item) {
	        return function () { return new OptionValue(item); };
	    }
	    Value.option = option;
	    function array(item) {
	        return function () { return new ArrayValue(item); };
	    }
	    Value.array = array;
	    function link(item, notFoundHandler) {
	        if (notFoundHandler === void 0) { notFoundHandler = NotFoundHandler.raiseNotFoundError(); }
	        return function () { return new LinkValue(item, notFoundHandler); };
	    }
	    Value.link = link;
	    function optionalLink(item) {
	        return function () { return new OptionValue(function () { return new LinkValue(item, NotFoundHandler.defaultTo(undefined)); }); };
	    }
	    Value.optionalLink = optionalLink;
	    function createChoice(description, items) {
	        return function () { return new ChoiceValue(description, items); };
	    }
	    Value.createChoice = createChoice;
	    function choice(description, items) {
	        return createChoice(description, items);
	    }
	    Value.choice = choice;
	    function object(jsConstructor, properties, extraPropertiesStrategy) {
	        if (extraPropertiesStrategy === void 0) { extraPropertiesStrategy = null; }
	        var eps = extraPropertiesStrategy !== null ? flib_1.Option.some(extraPropertiesStrategy) : flib_1.Option.None;
	        return function () { return new ObjectValue(jsConstructor, properties, eps); };
	    }
	    Value.object = object;
	})(Value = exports.Value || (exports.Value = {}));


/***/ },
/* 21 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var model_1 = __webpack_require__(20);
	var Mappings_1 = __webpack_require__(22);
	var flib_1 = __webpack_require__(5);
	function assert(msg, b) {
	    if (!b)
	        throw new Error(msg());
	}
	function checkConsistent(v, propTyp, errorPrefix) {
	    if (v === null || v === undefined)
	        throw new Error("null mapping");
	    var nop = function () { return null; };
	    model_1.foldCase(nop, nop, function () {
	        var errMsg = function () { return (errorPrefix() + ": expecting object got " + propTyp.name); };
	        assert(errMsg, !isPrimitive(propTyp));
	        assert(errMsg, propTyp !== flib_1.Option);
	        assert(errMsg, propTyp !== Array);
	    }, function () { return assert(function () { return (errorPrefix() + ": expecting option got " + propTyp.name); }, propTyp === flib_1.Option); }, function () { return assert(function () { return (errorPrefix() + ": expecting array got " + propTyp.name); }, propTyp === Array); }, nop)(v);
	}
	function isPrimitive(typ) {
	    return typ === Number || typ === Boolean || typ === String;
	}
	function makeLinkMapping(mp) {
	    if (mp instanceof model_1.OptionValue) {
	        var item_1 = mp.item();
	        if ((item_1 instanceof model_1.ObjectValue) || (item_1 instanceof model_1.ChoiceValue)) {
	            return model_1.Value.optionalLink(function () { return item_1; })();
	        }
	        else {
	            throw new Error("invalid link mapping " + mp);
	        }
	    }
	    else {
	        return model_1.Value.link(function () { return mp; })();
	    }
	}
	function link(mapping, sourceProperty) {
	    return function (target, targetProperty) {
	        var container = target.constructor;
	        var propType = Reflect.getMetadata("design:type", target, targetProperty);
	        var errPrefix = function () { return ("Property '" + targetProperty + "' of " + container.name); };
	        if (mapping === null || mapping === undefined) {
	            var cantInferType = function () { return (errPrefix() + ": cant infer link type"); };
	            var errMsg = function () { return (errPrefix() + ": expecting a link got " + propType.name); };
	            if (propType === undefined || propType === null)
	                throw new Error(cantInferType());
	            assert(errMsg, !isPrimitive(propType));
	            assert(errMsg, propType !== Array);
	            assert(cantInferType, propType !== flib_1.Option);
	        }
	        var mapping1 = (mapping === null || mapping === undefined) ? flib_1.lazy(function () { return Mappings_1.Mappings.instance.getMapping(propType); }) : mapping;
	        Mappings_1.Mappings.instance.addProperty(container, targetProperty, sourceProperty || targetProperty, function () {
	            var mpng = mapping1();
	            checkConsistent(mpng, propType, errPrefix);
	            return makeLinkMapping(mpng);
	        });
	    };
	}
	exports.link = link;
	function getMappingFromPropType(propType) {
	    if (propType === Number)
	        return model_1.Value.number();
	    else if (propType === Boolean)
	        return model_1.Value.boolean();
	    else if (propType === String)
	        return model_1.Value.string();
	    else
	        return Mappings_1.Mappings.instance.getMapping(propType);
	}
	function convert(mapping, sourceProperty) {
	    return function (target, targetProperty) {
	        var container = target.constructor;
	        var propType = Reflect.getMetadata("design:type", target, targetProperty);
	        var errPrefix = function () { return ("Property '" + targetProperty + "' of " + container.name); };
	        if (mapping === null || mapping === undefined) {
	            var cantInferType = function () { return (errPrefix() + ": cant infer type"); };
	            if (propType === undefined || propType === null)
	                throw new Error(cantInferType());
	            else if (propType === flib_1.Option)
	                throw new Error(cantInferType());
	            else if (propType === Array)
	                throw new Error(cantInferType());
	        }
	        var mapping1 = (mapping === null || mapping === undefined) ?
	            function () {
	                var mp1 = getMappingFromPropType(propType);
	                checkConsistent(mp1, propType, errPrefix);
	                return mp1;
	            } :
	            function () {
	                var mp1 = mapping();
	                checkConsistent(mp1, propType, errPrefix);
	                return mp1;
	            };
	        Mappings_1.Mappings.instance.addProperty(container, targetProperty, sourceProperty || targetProperty, mapping1);
	    };
	}
	exports.convert = convert;
	function fetchProperties(properties) {
	    return function (target) {
	        Mappings_1.Mappings.instance.setMappingProperties(target, properties);
	    };
	}
	exports.fetchProperties = fetchProperties;


/***/ },
/* 22 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var model_1 = __webpack_require__(20);
	var ObjectsCache_1 = __webpack_require__(23);
	var ObjectValueBuilder_1 = __webpack_require__(24);
	var Mappings = (function () {
	    function Mappings() {
	        this.buildersMap = ObjectsCache_1.newObjectsCache();
	        this.mappingsMap = ObjectsCache_1.newObjectsCache();
	    }
	    Mappings.defaultMissingMapping = function () {
	        return model_1.Value.object(Object, {}, model_1.ExtraPropertiesStrategy.copy);
	    };
	    Mappings.prototype.addProperty = function (container, targetProperty, sourceProperty, mapping) {
	        var _this = this;
	        var nm = (container.name !== null && container.name !== undefined) ? container.name : "";
	        var bldr = this.buildersMap.get(nm, container).fold(function () {
	            var newObjBuilder = new ObjectValueBuilder_1.default(container, {}, Mappings.defaultExtraPropertiesStrategy);
	            _this.buildersMap.put(nm, container, newObjBuilder);
	            return newObjBuilder;
	        }, function (bldr) { return bldr; });
	        bldr.add(targetProperty, sourceProperty, mapping);
	    };
	    Mappings.prototype.setMappingProperties = function (container, properties) {
	        var _this = this;
	        var nm = (container.name !== null && container.name !== undefined) ? container.name : "";
	        this.buildersMap.get(nm, container).fold(function () {
	            _this.buildersMap.put(nm, container, new ObjectValueBuilder_1.default(container, {}, properties.extraPropertiesStrategy));
	        }, function (bldr) {
	            bldr.setExtraPropertiesStrategy(properties.extraPropertiesStrategy);
	        });
	    };
	    Mappings.prototype.getMapping = function (container) {
	        var _this = this;
	        var nm = (container.name !== null && container.name !== undefined) ? container.name : "";
	        return this.mappingsMap.get(nm, container).fold(function () { return _this.buildersMap.get(nm, container).fold(function () { return model_1.Value.object(container, {}, model_1.ExtraPropertiesStrategy.copy)(); }, function (bldr) {
	            var r = bldr.build();
	            _this.mappingsMap.put(nm, container, r);
	            return r;
	        }); }, function (v) { return v; });
	    };
	    Mappings.instance = new Mappings();
	    Mappings.defaultExtraPropertiesStrategy = model_1.ExtraPropertiesStrategy.copy;
	    return Mappings;
	}());
	exports.Mappings = Mappings;
	var Lazy;
	(function (Lazy) {
	    function mapping(jsConstructor) {
	        return function () {
	            var cnstr = jsConstructor();
	            if (cnstr === undefined || cnstr === undefined)
	                throw new Error("null mapping");
	            return Mappings.instance.getMapping(cnstr);
	        };
	    }
	    Lazy.mapping = mapping;
	    function arrayOf(jsConstructor) {
	        return model_1.Value.array(mapping(jsConstructor));
	    }
	    Lazy.arrayOf = arrayOf;
	    function optionOf(jsConstructor) {
	        return model_1.Value.option(mapping(jsConstructor));
	    }
	    Lazy.optionOf = optionOf;
	    function arrayOfLinks(jsConstructor) {
	        return model_1.Value.array(model_1.Value.link(mapping(jsConstructor)));
	    }
	    Lazy.arrayOfLinks = arrayOfLinks;
	    function optionalLink(jsConstructor) {
	        return model_1.Value.option(model_1.Value.link(mapping(jsConstructor)));
	    }
	    Lazy.optionalLink = optionalLink;
	    function choose(description) {
	        var p = [];
	        for (var _i = 1; _i < arguments.length; _i++) {
	            p[_i - 1] = arguments[_i];
	        }
	        var cases = p.map(function (p) {
	            var pred = p[0], constr = p[1];
	            return model_1.Value.match(pred)(mapping(constr));
	        });
	        return model_1.Value.choice(description, cases);
	    }
	    Lazy.choose = choose;
	})(Lazy = exports.Lazy || (exports.Lazy = {}));
	function mapping(jsConstructor) {
	    return Lazy.mapping(function () { return jsConstructor; });
	}
	exports.mapping = mapping;
	function arrayOf(jsConstructor) {
	    return Lazy.arrayOf(function () { return jsConstructor; });
	}
	exports.arrayOf = arrayOf;
	function arrayOfLinks(jsConstructor) {
	    return Lazy.arrayOfLinks(function () { return jsConstructor; });
	}
	exports.arrayOfLinks = arrayOfLinks;
	function optionalLink(jsConstructor) {
	    return Lazy.optionalLink(function () { return jsConstructor; });
	}
	exports.optionalLink = optionalLink;
	function optionOf(jsConstructor) {
	    return Lazy.optionOf(function () { return jsConstructor; });
	}
	exports.optionOf = optionOf;
	function choose(description) {
	    var ps = [];
	    for (var _i = 1; _i < arguments.length; _i++) {
	        ps[_i - 1] = arguments[_i];
	    }
	    var cases = ps.map(function (p) {
	        var pred = p[0], jsc = p[1];
	        var r = [pred, function () { return jsc; }];
	        return r;
	    });
	    return Lazy.choose.apply(Lazy, [description].concat(cases));
	}
	exports.choose = choose;


/***/ },
/* 23 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	function newObjectsCache() {
	    return new EntriesMap(function (a, b) { return a === b; });
	}
	exports.newObjectsCache = newObjectsCache;
	var EntriesMap = (function () {
	    function EntriesMap(keyPredicate) {
	        this.keyPredicate = keyPredicate;
	        this.cache = {};
	    }
	    EntriesMap.prototype.get = function (ks, k) {
	        var _this = this;
	        return new flib_1.Option(this.cache[ks]).flatMap(function (entries) {
	            return flib_1.Arrays.find(entries, function (e) { return _this.keyPredicate(e.key, k); });
	        }).map(function (e) { return e.value; });
	    };
	    EntriesMap.prototype.put = function (ks, k, v) {
	        var _this = this;
	        new flib_1.Option(this.cache[ks]).fold(function () { _this.cache[ks] = [{ key: k, value: v }]; }, function (entries) {
	            flib_1.Arrays.find(entries, function (e) { return _this.keyPredicate(e.key, k); }).fold(function () { return entries.push({ key: k, value: v }); }, function (oldValue) {
	                oldValue.value = v;
	                //throw new Error(`replacing previous entry ${ks} key ${k}`)
	            });
	        });
	    };
	    return EntriesMap;
	}());


/***/ },
/* 24 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var model_1 = __webpack_require__(20);
	var ObjectValueBuilder = (function () {
	    function ObjectValueBuilder(jsConstructor, properties, extraPropertiesStrategy) {
	        this.jsConstructor = jsConstructor;
	        this.properties = properties;
	        this.extraPropertiesStrategy = extraPropertiesStrategy;
	    }
	    ObjectValueBuilder.prototype.setExtraPropertiesStrategy = function (eps) {
	        this.extraPropertiesStrategy = eps;
	    };
	    ObjectValueBuilder.prototype.add = function (targetProperty, sourceProperty, mapping) {
	        if (this.result)
	            throw new Error("cannot add more properties");
	        this.properties[targetProperty] = [sourceProperty, mapping];
	    };
	    ObjectValueBuilder.prototype.build = function () {
	        if (!this.result)
	            this.result = model_1.Value.object(this.jsConstructor, this.properties, this.extraPropertiesStrategy)();
	        return this.result;
	    };
	    return ObjectValueBuilder;
	}());
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = ObjectValueBuilder;


/***/ },
/* 25 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var ObjectsCache_1 = __webpack_require__(23);
	var model_1 = __webpack_require__(20);
	function getJsonValue(v, desc, predicate) {
	    if (flib_1.isNull(v))
	        return flib_1.Try.failure(new Error("null"));
	    else if (!predicate(v))
	        return flib_1.Try.failure(new Error(JSON.stringify(v) + " is not " + desc));
	    else
	        return flib_1.Try.success(v);
	}
	function getJsonProperty(json, name) {
	    return (json === undefined || typeof json !== "object") ?
	        flib_1.Try.failure(new Error("error while reading Value " + name + ", expecting an object got " + JSON.stringify(json))) :
	        flib_1.Try.success(json[name]);
	}
	var InterpreterResult = (function () {
	    function InterpreterResult(cycles) {
	        this.cycles = cycles;
	    }
	    InterpreterResult.merge = function (ress) {
	        return ress.reduce(function (acc, itm) { return acc.add(itm); }, InterpreterResult.empty);
	    };
	    InterpreterResult.prototype.add = function (p) {
	        return new InterpreterResult(this.cycles.concat(p.cycles));
	    };
	    InterpreterResult.prototype.all = function () {
	        return Promise.all(this.cycles);
	    };
	    InterpreterResult.empty = new InterpreterResult([]);
	    return InterpreterResult;
	}());
	var Context = (function () {
	    function Context(extraPropertiesStrategy, httpGet, objectsCache, setValue, url, parentUrl) {
	        this.extraPropertiesStrategy = extraPropertiesStrategy;
	        this.httpGet = httpGet;
	        this.objectsCache = objectsCache;
	        this.setValue = setValue;
	        this.url = url;
	        this.parentUrl = parentUrl;
	    }
	    Context.prototype.withUrl = function (url) {
	        return new Context(this.extraPropertiesStrategy, this.httpGet, this.objectsCache, this.setValue, flib_1.Option.some(url), this.url);
	    };
	    Context.prototype.withoutUrl = function () {
	        return new Context(this.extraPropertiesStrategy, this.httpGet, this.objectsCache, this.setValue, flib_1.Option.None, this.url);
	    };
	    Context.prototype.withSetValue = function (setValue) {
	        return new Context(this.extraPropertiesStrategy, this.httpGet, this.objectsCache, setValue, this.url, this.parentUrl);
	    };
	    return Context;
	}());
	var ResourceFetch = (function () {
	    function ResourceFetch(propertiesWithoutMappingStrategy, httpCacheFactory) {
	        this.propertiesWithoutMappingStrategy = propertiesWithoutMappingStrategy;
	        this.httpCacheFactory = httpCacheFactory;
	    }
	    ResourceFetch.prototype.fetchResource = function (url, mapping) {
	        return this.fetchRun(url, model_1.Value.link(mapping)());
	    };
	    ResourceFetch.prototype.fetchObject = function (obj, mapping) {
	        return this.fetchRun(obj, mapping());
	    };
	    ResourceFetch.prototype.fetchRun = function (obj, mapping) {
	        var interpreter = new JsonIntepreter();
	        var httpGet = this.httpCacheFactory();
	        var result;
	        var ctx = new Context(this.propertiesWithoutMappingStrategy, httpGet, ObjectsCache_1.newObjectsCache(), function (v) { return result = v; }, flib_1.Option.None, flib_1.Option.None);
	        var ires = interpreter.interpret(ctx, mapping, obj);
	        return ires.then(function (ires) { return ires.all().then(function (v) { return result; }); });
	    };
	    return ResourceFetch;
	}());
	exports.ResourceFetch = ResourceFetch;
	function isGetPropertyValue(v) {
	    var no = function () { return false; };
	    var yes = function () { return true; };
	    return v.fold(function (item) { return item.fold(no, no, no, yes); }, no, no, no, no, no);
	}
	function simpleValue(json, typename, f, setValue) {
	    var tr = getJsonValue(json, typename, function (a) { return (typeof a === typename); });
	    return tr.fold(function (err) { return Promise.reject(err); }, function (v) {
	        setValue(f(v));
	        return Promise.resolve(InterpreterResult.empty);
	    });
	}
	function setPropertyValue(a, p, v) {
	    try {
	        a[p] = v;
	    }
	    catch (e) {
	    }
	}
	var JsonIntepreter = (function () {
	    function JsonIntepreter() {
	    }
	    JsonIntepreter.prototype.interpret = function (context, mapping, json) {
	        var _this = this;
	        return mapping.fold(function (rawValue) {
	            return rawValue.fold(function (f) { return simpleValue(json, "boolean", f, context.setValue); }, function (f) { return simpleValue(json, "number", f, context.setValue); }, function (f) { return simpleValue(json, "string", f, context.setValue); }, function (f) {
	                context.setValue(f(context.parentUrl));
	                return Promise.resolve(InterpreterResult.empty);
	            });
	        }, function (item, notFoundHandler) {
	            if (typeof json === "string") {
	                var url_1 = json;
	                return context.httpGet(url_1).then(function (json) {
	                    return json.fold(function () {
	                        context.setValue(notFoundHandler(url_1));
	                        return Promise.resolve(InterpreterResult.empty);
	                    }, function (json) { return _this.interpret(context.withUrl(url_1), item, json); });
	                });
	            }
	            else {
	                return Promise.reject(new Error(JSON.stringify(json) + " is not a url"));
	            }
	        }, function (jsConstr, props, optExtraPropertiesStrategy) {
	            var cachedObj = context.url.flatMap(function (url) { return context.objectsCache.get(url, jsConstr); });
	            return cachedObj.fold(function () {
	                var resObj = new jsConstr();
	                var res = Promise.resolve(resObj);
	                context.url.forEach(function (url) { return context.objectsCache.put(url, jsConstr, res); });
	                context.setValue(resObj);
	                var remainingJsonPropertiesSet = flib_1.JsMap.map(json, function (k, v) { return true; });
	                var promisesMap = flib_1.JsMap.map(props, function (targetProperty, nameProp) {
	                    var sourceProperty = nameProp[0], prop = nameProp[1];
	                    delete remainingJsonPropertiesSet[sourceProperty];
	                    return getJsonProperty(json, sourceProperty).fold(function (err) { return Promise.reject(err); }, function (v) { return _this.interpret(context.withoutUrl().withSetValue(function (v) { return setPropertyValue(resObj, targetProperty, v); }), prop, v); });
	                });
	                var extraPropsStrategy = optExtraPropertiesStrategy.getOrElse(function () { return context.extraPropertiesStrategy; });
	                switch (extraPropsStrategy) {
	                    case model_1.ExtraPropertiesStrategy.fail:
	                        if (Object.keys(remainingJsonPropertiesSet).length > 0)
	                            return Promise.reject(new Error("extra properties " + Object.keys(remainingJsonPropertiesSet) + " creating object " + jsConstr.name));
	                    case model_1.ExtraPropertiesStrategy.copy:
	                        flib_1.JsMap.forEach(remainingJsonPropertiesSet, function (k, v) { return setPropertyValue(resObj, k, json[k]); });
	                    case model_1.ExtraPropertiesStrategy.discard:
	                    default:
	                        var proms = Promise.all(Object.keys(promisesMap).map(function (k) { return promisesMap[k]; }));
	                        return proms.then(function (proms) { return InterpreterResult.merge(proms); });
	                }
	            }, function (v) {
	                v.then(context.setValue);
	                return Promise.resolve(new InterpreterResult([v]));
	            });
	        }, function (item) {
	            var interpretItem = function () { return _this.interpret(context.withSetValue(function (v) { return context.setValue(new flib_1.Option(v)); }), item, json); };
	            var setNone = function () {
	                context.setValue(flib_1.Option.None);
	                return Promise.resolve(InterpreterResult.empty);
	            };
	            var itemIsGetProperty = isGetPropertyValue(item);
	            if (itemIsGetProperty) {
	                try {
	                    return interpretItem();
	                }
	                catch (e) {
	                    return setNone();
	                }
	            }
	            else if (flib_1.isNull(json)) {
	                return setNone();
	            }
	            else {
	                return interpretItem();
	            }
	        }, function (item) {
	            if (Array.isArray(json)) {
	                var resArray_1 = [];
	                context.setValue(resArray_1);
	                var proms = json.map(function (i, index) {
	                    return _this.interpret(context.withSetValue(function (v) { return resArray_1[index] = v; }), item, i);
	                });
	                return Promise.all(proms).then(function (proms) { return InterpreterResult.merge(proms); });
	            }
	            else
	                return Promise.reject(new Error("expecting an array got " + JSON.stringify(json)));
	        }, function (description, choices) {
	            if (!flib_1.isNull(json)) {
	                return _this.findChoice(context, json, description, choices);
	            }
	            else {
	                return Promise.reject(new Error("null"));
	            }
	        });
	    };
	    JsonIntepreter.prototype.findChoice = function (context, json, description, choices) {
	        var _this = this;
	        var cantFindChoice = function () { return Promise.reject(new Error("could not find a valid choice for " + description + "\ninput:\n" + JSON.stringify(json, null, "  "))); };
	        return flib_1.Arrays.findIndex(choices, function (choice) { return choice.predicate(json); }).fold(cantFindChoice, function (idx) { return _this.interpret(context, choices[idx].value(), json); });
	    };
	    return JsonIntepreter;
	}());


/***/ },
/* 26 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var HttpCacheFactory = (function () {
	    function HttpCacheFactory(requestFactory, responseReader) {
	        this.requestFactory = requestFactory;
	        this.responseReader = responseReader;
	    }
	    HttpCacheFactory.prototype.create = function () {
	        var _this = this;
	        var cache = {};
	        return function (url) {
	            if (url === undefined || url == null)
	                throw new Error("undefined url");
	            var cr = cache[url];
	            if (cr !== null && cr !== undefined)
	                return cr;
	            else {
	                var req_1 = _this.requestFactory(url);
	                var r = window.fetch(url, req_1).then(function (resp) {
	                    if (resp.status === 404)
	                        return Promise.resolve(flib_1.Option.None);
	                    else if (resp.status >= 200 && resp.status < 300)
	                        return _this.responseReader(resp, req_1).then(function (resp) { return flib_1.Option.option(resp); });
	                    else
	                        return Promise.reject(new Error("GET " + url + ":\n" + JSON.stringify(resp, null, 2)));
	                });
	                cache[url] = r;
	                return r;
	            }
	        };
	    };
	    return HttpCacheFactory;
	}());
	function httpCacheFactory(requestFactory, responseReader) {
	    var reqFactory = new HttpCacheFactory(requestFactory, responseReader);
	    return function () { return reqFactory.create(); };
	}
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = httpCacheFactory;


/***/ },
/* 27 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var ResourceFetch_1 = __webpack_require__(25);
	var TestFetcher = (function () {
	    function TestFetcher(extraPropertiesStrategy) {
	        this.extraPropertiesStrategy = extraPropertiesStrategy;
	    }
	    TestFetcher.promisesMap = function (mp) {
	        return function (k) {
	            var v = mp[k];
	            if (v === undefined || v === null)
	                throw new Error("missing: " + k);
	            else
	                return Promise.resolve(new flib_1.Option(v));
	        };
	    };
	    TestFetcher.prototype.fetch = function (url, testMapping, cache) {
	        var rf = new ResourceFetch_1.ResourceFetch(this.extraPropertiesStrategy, function () { return cache; });
	        return rf.fetchResource(url, testMapping);
	    };
	    TestFetcher.prototype.fetchObj = function (obj, testMapping, cache) {
	        var rf = new ResourceFetch_1.ResourceFetch(this.extraPropertiesStrategy, function () { return cache; });
	        return rf.fetchObject(obj, testMapping);
	    };
	    TestFetcher.prototype.fetchResource = function (url, testMapping, cache) {
	        var _this = this;
	        return function (f) {
	            _this.fetch(url, testMapping, cache).then(f, function (err) {
	                console.log(err.stack);
	                fail("error getting url " + url + " " + err);
	            });
	        };
	    };
	    TestFetcher.prototype.fetchFails = function (url, testMapping, cache) {
	        var _this = this;
	        return function (f) {
	            _this.fetch(url, testMapping, cache).then(function (v) { throw new Error("expecting a failure while getting url ${url}"); }, f);
	        };
	    };
	    TestFetcher.prototype.fetchObject = function (obj, testMapping, cache) {
	        var _this = this;
	        return function (f) {
	            _this.fetchObj(obj, testMapping, cache).then(f, function (err) {
	                console.log(err.stack);
	                fail("error getting object " + JSON.stringify(obj, null, 2) + " " + err);
	            });
	        };
	    };
	    TestFetcher.prototype.fetchObjectFails = function (obj, testMapping, cache) {
	        var _this = this;
	        return function (f) {
	            _this.fetchObj(obj, testMapping, cache).then(function (v) { throw new Error("expecting a failure while getting object  ${JSON.stringify(obj, null, 2)}"); }, f);
	        };
	    };
	    return TestFetcher;
	}());
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = TestFetcher;


/***/ },
/* 28 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	function toEnum(a, desc) {
	    return valueFrom(a, desc);
	}
	exports.toEnum = toEnum;
	function valueFrom(mp, desc) {
	    return function (str) {
	        var r = mp[str];
	        if (flib_1.isNull(r))
	            throw Error(str + " is not a " + desc);
	        return r;
	    };
	}
	exports.valueFrom = valueFrom;


/***/ },
/* 29 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
	    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
	    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
	    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
	    return c > 3 && r && Object.defineProperty(target, key, r), r;
	};
	var __metadata = (this && this.__metadata) || function (k, v) {
	    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
	};
	var flib_1 = __webpack_require__(5);
	var competition_1 = __webpack_require__(17);
	var player_1 = __webpack_require__(18);
	var nrest_fetch_1 = __webpack_require__(19);
	(function (CompetitionEventKind) {
	    CompetitionEventKind[CompetitionEventKind["createdCompetition"] = 0] = "createdCompetition";
	    CompetitionEventKind[CompetitionEventKind["confirmedCompetition"] = 1] = "confirmedCompetition";
	    CompetitionEventKind[CompetitionEventKind["playerAccepted"] = 2] = "playerAccepted";
	    CompetitionEventKind[CompetitionEventKind["playerDeclined"] = 3] = "playerDeclined";
	})(exports.CompetitionEventKind || (exports.CompetitionEventKind = {}));
	var CompetitionEventKind = exports.CompetitionEventKind;
	var CompetitionDeclined = (function () {
	    function CompetitionDeclined() {
	    }
	    Object.defineProperty(CompetitionDeclined.prototype, "eventName", {
	        get: function () {
	            return CompetitionEventKind[CompetitionEventKind.playerDeclined];
	        },
	        enumerable: true,
	        configurable: true
	    });
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], CompetitionDeclined.prototype, "player", void 0);
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', competition_1.CompetitionState)
	    ], CompetitionDeclined.prototype, "competition", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.Value.option(nrest_fetch_1.Value.string)), 
	        __metadata('design:type', flib_1.Option)
	    ], CompetitionDeclined.prototype, "reason", void 0);
	    return CompetitionDeclined;
	}());
	exports.CompetitionDeclined = CompetitionDeclined;
	var CompetitionAccepted = (function () {
	    function CompetitionAccepted() {
	    }
	    Object.defineProperty(CompetitionAccepted.prototype, "eventName", {
	        get: function () {
	            return CompetitionEventKind[CompetitionEventKind.playerAccepted];
	        },
	        enumerable: true,
	        configurable: true
	    });
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], CompetitionAccepted.prototype, "player", void 0);
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', competition_1.CompetitionState)
	    ], CompetitionAccepted.prototype, "competition", void 0);
	    return CompetitionAccepted;
	}());
	exports.CompetitionAccepted = CompetitionAccepted;
	var ConfirmedCompetition = (function () {
	    function ConfirmedCompetition() {
	    }
	    Object.defineProperty(ConfirmedCompetition.prototype, "eventName", {
	        get: function () {
	            return CompetitionEventKind[CompetitionEventKind.confirmedCompetition];
	        },
	        enumerable: true,
	        configurable: true
	    });
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', competition_1.CompetitionState)
	    ], ConfirmedCompetition.prototype, "competition", void 0);
	    return ConfirmedCompetition;
	}());
	exports.ConfirmedCompetition = ConfirmedCompetition;
	var CreatedCompetition = (function () {
	    function CreatedCompetition() {
	    }
	    Object.defineProperty(CreatedCompetition.prototype, "eventName", {
	        get: function () {
	            return CompetitionEventKind[CompetitionEventKind.createdCompetition];
	        },
	        enumerable: true,
	        configurable: true
	    });
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], CreatedCompetition.prototype, "issuer", void 0);
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', competition_1.CompetitionState)
	    ], CreatedCompetition.prototype, "competition", void 0);
	    return CreatedCompetition;
	}());
	exports.CreatedCompetition = CreatedCompetition;
	exports.competitionEvents = [
	    [
	        function (a) { return a.kind === CompetitionEventKind[CompetitionEventKind.createdCompetition]; },
	        function () { return CreatedCompetition; }
	    ], [
	        function (a) { return a.kind === CompetitionEventKind[CompetitionEventKind.confirmedCompetition]; },
	        function () { return ConfirmedCompetition; }
	    ], [
	        function (a) { return a.kind === CompetitionEventKind[CompetitionEventKind.playerAccepted]; },
	        function () { return CompetitionAccepted; }
	    ], [
	        function (a) { return a.kind === CompetitionEventKind[CompetitionEventKind.playerDeclined]; },
	        function () { return CompetitionDeclined; }
	    ]
	];
	exports.competitionEventChoice = nrest_fetch_1.Lazy.choose.apply(nrest_fetch_1.Lazy, ["CompetitionEvent"].concat(exports.competitionEvents));


/***/ },
/* 30 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
	    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
	    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
	    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
	    return c > 3 && r && Object.defineProperty(target, key, r), r;
	};
	var __metadata = (this && this.__metadata) || function (k, v) {
	    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
	};
	var flib_1 = __webpack_require__(5);
	var player_1 = __webpack_require__(18);
	var nrest_fetch_1 = __webpack_require__(19);
	var Util_1 = __webpack_require__(28);
	(function (GameStateKind) {
	    GameStateKind[GameStateKind["active"] = 0] = "active";
	    GameStateKind[GameStateKind["dropped"] = 1] = "dropped";
	    GameStateKind[GameStateKind["finished"] = 2] = "finished";
	})(exports.GameStateKind || (exports.GameStateKind = {}));
	var GameStateKind = exports.GameStateKind;
	(function (Seed) {
	    Seed[Seed["bastoni"] = 0] = "bastoni";
	    Seed[Seed["coppe"] = 1] = "coppe";
	    Seed[Seed["denari"] = 2] = "denari";
	    Seed[Seed["spade"] = 3] = "spade";
	})(exports.Seed || (exports.Seed = {}));
	var Seed = exports.Seed;
	(function (DropReasonKind) {
	    DropReasonKind[DropReasonKind["playerLeft"] = 0] = "playerLeft";
	})(exports.DropReasonKind || (exports.DropReasonKind = {}));
	var DropReasonKind = exports.DropReasonKind;
	var stringToSeed = nrest_fetch_1.Value.string().map(Util_1.toEnum(Seed, "Seed"));
	var Card = (function () {
	    function Card() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(function () { return stringToSeed; }), 
	        __metadata('design:type', Number)
	    ], Card.prototype, "seed", void 0);
	    return Card;
	}());
	exports.Card = Card;
	var Move = (function () {
	    function Move() {
	    }
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], Move.prototype, "player", void 0);
	    __decorate([
	        nrest_fetch_1.convert(), 
	        __metadata('design:type', Card)
	    ], Move.prototype, "card", void 0);
	    return Move;
	}());
	exports.Move = Move;
	var PlayerScore = (function () {
	    function PlayerScore() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOf(Card)), 
	        __metadata('design:type', Array)
	    ], PlayerScore.prototype, "cards", void 0);
	    return PlayerScore;
	}());
	exports.PlayerScore = PlayerScore;
	var PlayerFinalState = (function () {
	    function PlayerFinalState() {
	    }
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], PlayerFinalState.prototype, "player", void 0);
	    __decorate([
	        nrest_fetch_1.convert(), 
	        __metadata('design:type', PlayerScore)
	    ], PlayerFinalState.prototype, "score", void 0);
	    return PlayerFinalState;
	}());
	exports.PlayerFinalState = PlayerFinalState;
	var PlayerState = (function () {
	    function PlayerState() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.Value.getUrl), 
	        __metadata('design:type', String)
	    ], PlayerState.prototype, "self", void 0);
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], PlayerState.prototype, "player", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOf(Card)), 
	        __metadata('design:type', Array)
	    ], PlayerState.prototype, "cards", void 0);
	    __decorate([
	        nrest_fetch_1.convert(), 
	        __metadata('design:type', PlayerScore)
	    ], PlayerState.prototype, "score", void 0);
	    return PlayerState;
	}());
	exports.PlayerState = PlayerState;
	var PlayersGameResult = (function () {
	    function PlayersGameResult() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOf(PlayerFinalState)), 
	        __metadata('design:type', Array)
	    ], PlayersGameResult.prototype, "playersOrderByPoints", void 0);
	    __decorate([
	        nrest_fetch_1.convert(), 
	        __metadata('design:type', PlayerFinalState)
	    ], PlayersGameResult.prototype, "winner", void 0);
	    return PlayersGameResult;
	}());
	exports.PlayersGameResult = PlayersGameResult;
	var TeamScore = (function () {
	    function TeamScore() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOfLinks(player_1.Player)), 
	        __metadata('design:type', Array)
	    ], TeamScore.prototype, "players", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOf(Card)), 
	        __metadata('design:type', Array)
	    ], TeamScore.prototype, "cards", void 0);
	    return TeamScore;
	}());
	exports.TeamScore = TeamScore;
	var TeamsGameResult = (function () {
	    function TeamsGameResult() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOf(TeamScore)), 
	        __metadata('design:type', Array)
	    ], TeamsGameResult.prototype, "teamsOrderByPoints", void 0);
	    __decorate([
	        nrest_fetch_1.convert(), 
	        __metadata('design:type', TeamScore)
	    ], TeamsGameResult.prototype, "winnerTeam", void 0);
	    return TeamsGameResult;
	}());
	exports.TeamsGameResult = TeamsGameResult;
	var FinalGameState = (function () {
	    function FinalGameState() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(), 
	        __metadata('design:type', Card)
	    ], FinalGameState.prototype, "briscolaCard", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.choose("GameResult", [
	            function (a) { return !flib_1.isNull(a.playersOrderByPoints); },
	            PlayersGameResult
	        ], [
	            function (a) { return !flib_1.isNull(a.teamsOrderByPoints); },
	            TeamsGameResult
	        ])), 
	        __metadata('design:type', Object)
	    ], FinalGameState.prototype, "gameResult", void 0);
	    return FinalGameState;
	}());
	exports.FinalGameState = FinalGameState;
	var ActiveGameState = (function () {
	    function ActiveGameState() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(), 
	        __metadata('design:type', Card)
	    ], ActiveGameState.prototype, "briscolaCard", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOf(Move)), 
	        __metadata('design:type', Array)
	    ], ActiveGameState.prototype, "moves", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOfLinks(player_1.Player)), 
	        __metadata('design:type', Array)
	    ], ActiveGameState.prototype, "nextPlayers", void 0);
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', player_1.CurrentPlayer)
	    ], ActiveGameState.prototype, "currentPlayer", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOfLinks(player_1.Player)), 
	        __metadata('design:type', Array)
	    ], ActiveGameState.prototype, "players", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.optionalLink(PlayerState)), 
	        __metadata('design:type', flib_1.Option)
	    ], ActiveGameState.prototype, "playerState", void 0);
	    return ActiveGameState;
	}());
	exports.ActiveGameState = ActiveGameState;
	var DropReason = (function () {
	    function DropReason() {
	    }
	    return DropReason;
	}());
	exports.DropReason = DropReason;
	var PlayerLeft = (function (_super) {
	    __extends(PlayerLeft, _super);
	    function PlayerLeft() {
	        _super.apply(this, arguments);
	    }
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], PlayerLeft.prototype, "player", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.Value.option(nrest_fetch_1.Value.string)), 
	        __metadata('design:type', flib_1.Option)
	    ], PlayerLeft.prototype, "reason", void 0);
	    return PlayerLeft;
	}(DropReason));
	exports.PlayerLeft = PlayerLeft;
	var DroppedGameState = (function () {
	    function DroppedGameState() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOfLinks(player_1.Player)), 
	        __metadata('design:type', Array)
	    ], DroppedGameState.prototype, "nextPlayers", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.mapping(PlayerLeft)), 
	        __metadata('design:type', DropReason)
	    ], DroppedGameState.prototype, "dropReason", void 0);
	    return DroppedGameState;
	}());
	exports.DroppedGameState = DroppedGameState;
	exports.gameStateChoice = nrest_fetch_1.Lazy.choose("GameState", [
	    function (a) { return a.kind === GameStateKind[GameStateKind.active]; },
	    function () { return ActiveGameState; }
	], [
	    function (a) { return a.kind === GameStateKind[GameStateKind.dropped]; },
	    function () { return DroppedGameState; }
	], [
	    function (a) { return a.kind === GameStateKind[GameStateKind.finished]; },
	    function () { return FinalGameState; }
	]);
	var GameState;
	(function (GameState) {
	    function fold(p, activeGameState, finalGameState, droppedGameState) {
	        if (p instanceof ActiveGameState)
	            return activeGameState(p);
	        else if (p instanceof FinalGameState)
	            return finalGameState(p);
	        else if (p instanceof DroppedGameState)
	            return droppedGameState(p);
	        else {
	            console.log("unrecognized GameState");
	            console.log(p);
	            throw new Error("unrecognized GameState ");
	        }
	    }
	    GameState.fold = fold;
	})(GameState = exports.GameState || (exports.GameState = {}));


/***/ },
/* 31 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
	    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
	    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
	    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
	    return c > 3 && r && Object.defineProperty(target, key, r), r;
	};
	var __metadata = (this && this.__metadata) || function (k, v) {
	    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
	};
	var game_1 = __webpack_require__(30);
	var nrest_fetch_1 = __webpack_require__(19);
	var player_1 = __webpack_require__(18);
	(function (BriscolaEventKind) {
	    BriscolaEventKind[BriscolaEventKind["gameStarted"] = 0] = "gameStarted";
	    BriscolaEventKind[BriscolaEventKind["cardPlayed"] = 1] = "cardPlayed";
	    BriscolaEventKind[BriscolaEventKind["gameDropped"] = 2] = "gameDropped";
	})(exports.BriscolaEventKind || (exports.BriscolaEventKind = {}));
	var BriscolaEventKind = exports.BriscolaEventKind;
	var CardPlayed = (function () {
	    function CardPlayed() {
	    }
	    Object.defineProperty(CardPlayed.prototype, "eventName", {
	        get: function () {
	            return BriscolaEventKind[BriscolaEventKind.cardPlayed];
	        },
	        enumerable: true,
	        configurable: true
	    });
	    __decorate([
	        nrest_fetch_1.link(game_1.gameStateChoice), 
	        __metadata('design:type', Object)
	    ], CardPlayed.prototype, "game", void 0);
	    __decorate([
	        nrest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], CardPlayed.prototype, "player", void 0);
	    __decorate([
	        nrest_fetch_1.convert(), 
	        __metadata('design:type', game_1.Card)
	    ], CardPlayed.prototype, "card", void 0);
	    return CardPlayed;
	}());
	exports.CardPlayed = CardPlayed;
	var GameStarted = (function () {
	    function GameStarted() {
	    }
	    Object.defineProperty(GameStarted.prototype, "eventName", {
	        get: function () {
	            return BriscolaEventKind[BriscolaEventKind.gameStarted];
	        },
	        enumerable: true,
	        configurable: true
	    });
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.mapping(game_1.ActiveGameState)), 
	        __metadata('design:type', game_1.ActiveGameState)
	    ], GameStarted.prototype, "game", void 0);
	    return GameStarted;
	}());
	exports.GameStarted = GameStarted;
	var GameDropped = (function () {
	    function GameDropped() {
	    }
	    Object.defineProperty(GameDropped.prototype, "eventName", {
	        get: function () {
	            return BriscolaEventKind[BriscolaEventKind.gameDropped];
	        },
	        enumerable: true,
	        configurable: true
	    });
	    __decorate([
	        nrest_fetch_1.link(game_1.gameStateChoice), 
	        __metadata('design:type', Object)
	    ], GameDropped.prototype, "game", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.mapping(game_1.PlayerLeft)), 
	        __metadata('design:type', game_1.DropReason)
	    ], GameDropped.prototype, "dropReason", void 0);
	    return GameDropped;
	}());
	exports.GameDropped = GameDropped;
	exports.gameEvents = [[
	        function (a) { return a.kind === BriscolaEventKind[BriscolaEventKind.gameStarted]; },
	        function () { return GameStarted; }
	    ], [
	        function (a) { return a.kind === BriscolaEventKind[BriscolaEventKind.cardPlayed]; },
	        function () { return CardPlayed; }
	    ], [
	        function (a) { return a.kind === BriscolaEventKind[BriscolaEventKind.gameDropped]; },
	        function () { return GameDropped; }
	    ]];
	exports.briscolaEventChoice = nrest_fetch_1.Lazy.choose.apply(nrest_fetch_1.Lazy, ["GameEvent"].concat(exports.gameEvents));


/***/ },
/* 32 */
/***/ function(module, exports) {

	"use strict";
	var SiteMap = (function () {
	    function SiteMap() {
	    }
	    return SiteMap;
	}());
	exports.SiteMap = SiteMap;


/***/ },
/* 33 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
	    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
	    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
	    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
	    return c > 3 && r && Object.defineProperty(target, key, r), r;
	};
	var __metadata = (this && this.__metadata) || function (k, v) {
	    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
	};
	var flib_1 = __webpack_require__(5);
	var game_1 = __webpack_require__(30);
	var gameEvents_1 = __webpack_require__(31);
	var competition_1 = __webpack_require__(17);
	var competitionEvents_1 = __webpack_require__(29);
	var player_1 = __webpack_require__(18);
	var nrest_fetch_1 = __webpack_require__(19);
	exports.eventAndStateChoice = nrest_fetch_1.Lazy.choose("EventAndState", [
	    function (wso) { return wso.event && eventMatch(player_1.playerEvents, wso.event); },
	    function () { return PlayerEventAndState; }
	], [
	    function (wso) { return wso.event && eventMatch(gameEvents_1.gameEvents, wso.event); },
	    function () { return GameEventAndState; }
	], [
	    function (wso) { return wso.event && eventMatch(competitionEvents_1.competitionEvents, wso.event); },
	    function () { return CompetitionEventAndState; }
	]);
	function eventMatch(preds, a) {
	    return flib_1.Arrays.exists(preds, function (p) { return p[0](a); });
	}
	var PlayerEventAndState = (function () {
	    function PlayerEventAndState() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(player_1.playerEventChoice), 
	        __metadata('design:type', Object)
	    ], PlayerEventAndState.prototype, "event", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.arrayOf(player_1.Player), "state"), 
	        __metadata('design:type', Array)
	    ], PlayerEventAndState.prototype, "players", void 0);
	    return PlayerEventAndState;
	}());
	exports.PlayerEventAndState = PlayerEventAndState;
	var GameEventAndState = (function () {
	    function GameEventAndState() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(gameEvents_1.briscolaEventChoice), 
	        __metadata('design:type', Object)
	    ], GameEventAndState.prototype, "event", void 0);
	    __decorate([
	        nrest_fetch_1.convert(game_1.gameStateChoice, "state"), 
	        __metadata('design:type', Object)
	    ], GameEventAndState.prototype, "game", void 0);
	    return GameEventAndState;
	}());
	exports.GameEventAndState = GameEventAndState;
	var CompetitionEventAndState = (function () {
	    function CompetitionEventAndState() {
	    }
	    __decorate([
	        nrest_fetch_1.convert(competitionEvents_1.competitionEventChoice), 
	        __metadata('design:type', Object)
	    ], CompetitionEventAndState.prototype, "event", void 0);
	    __decorate([
	        nrest_fetch_1.convert(nrest_fetch_1.mapping(competition_1.CompetitionState), "state"), 
	        __metadata('design:type', competition_1.CompetitionState)
	    ], CompetitionEventAndState.prototype, "competition", void 0);
	    return CompetitionEventAndState;
	}());
	exports.CompetitionEventAndState = CompetitionEventAndState;


/***/ },
/* 34 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var game_1 = __webpack_require__(30);
	function card(m) {
	    return {
	        number: m.number,
	        seed: game_1.Seed[m.seed]
	    };
	}
	exports.card = card;


/***/ },
/* 35 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var Http;
	(function (Http) {
	    function fetchWithBody(mthd) {
	        return function (url, body) {
	            if (url === undefined || url === null)
	                throw new Error("making request at null url, body : " + body);
	            var bdy = (body !== undefined && body !== null) ? JSON.stringify(body) : undefined;
	            return window.fetch(url, {
	                method: mthd,
	                body: bdy
	            });
	        };
	    }
	    function POST(url, body) {
	        return fetchWithBody("POST")(url, body);
	    }
	    Http.POST = POST;
	    function PUT(url, body) {
	        return fetchWithBody("PUT")(url, body);
	    }
	    Http.PUT = PUT;
	    function DELETE(url) {
	        return window.fetch(url, {
	            method: "DELETE"
	        });
	    }
	    Http.DELETE = DELETE;
	    function parseJson(txt) {
	        try {
	            return Promise.resolve(JSON.parse(txt));
	        }
	        catch (e) {
	            return Promise.reject("Error parsing json " + e.message);
	        }
	    }
	    function statusOk(resp) {
	        var st = resp.status;
	        return (st >= 200 && st < 300);
	    }
	    function reqDesc(req) {
	        return req.method + " " + req.url;
	    }
	    Http.jsonResponseReader = function (resp, req) {
	        if (statusOk(resp)) {
	            return resp.text().then(parseJson);
	        }
	        else {
	            return Promise.reject(reqDesc(req) + " return status : " + resp.status);
	        }
	    };
	    function createRequestFactory(reqInit) {
	        return function (url) {
	            return new Request(url, reqInit);
	        };
	    }
	    Http.createRequestFactory = createRequestFactory;
	})(Http = exports.Http || (exports.Http = {}));
	function observableWebSocket(url, protocol) {
	    if (flib_1.isNull(url))
	        throw new Error("websocket error, url is undefined");
	    if (flib_1.isNull(WebSocket))
	        throw new Error("websocket error, WebSocket not supported in current environment");
	    var channel = new Rx.Subject();
	    var socket = !flib_1.isNull(protocol) ? new WebSocket(url, protocol) : new WebSocket(url);
	    function showEvent(e) {
	        console.log("receive from websocket");
	        console.log(e);
	    }
	    socket.onclose = showEvent;
	    socket.onerror = showEvent;
	    socket.onopen = showEvent;
	    socket.onmessage = function (e) {
	        showEvent(e);
	        channel.onNext(e);
	    };
	    return channel;
	}
	exports.observableWebSocket = observableWebSocket;
	/*
	function observableWebSocket1(url:string, protocol?: string | string[]): Rx.Observable<MessageEvent> {
	  const channel = new Rx.Subject<MessageEvent>()
	  const socket = protocol === undefined ? new WebSocket(url, protocol) : new WebSocket(url);
	
	  function showEvent(e:Event) {
	    console.log("receive from websocket")
	    console.log(e)
	  }
	
	  socket.onclose = (e) => {
	    showEvent(e)
	    channel.onCompleted()
	  }
	
	  socket.onerror = (e) => {
	    showEvent(e)
	    channel.onError(e)
	  }
	
	  socket.onopen = showEvent
	
	  socket.onmessage = e => {
	    showEvent(e)
	    channel.onNext(e)
	  }
	
	  return channel;
	}
	*/
	function rxCollect(obs, pf) {
	    return obs.flatMap(function (v) {
	        return pf(v).fold(function () { return Rx.Observable.empty(); }, function (v) { return Rx.Observable.just(v); });
	    });
	}
	exports.rxCollect = rxCollect;
	/*
	* adapted from https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Object/assign
	*/
	function objectAssign(target, source) {
	    if (target === undefined || target === null) {
	        throw new TypeError('Cannot convert undefined or null to object');
	    }
	    var output = Object(target);
	    if (source !== undefined && source !== null) {
	        for (var nextKey in source) {
	            if (source.hasOwnProperty(nextKey)) {
	                output[nextKey] = source[nextKey];
	            }
	        }
	    }
	    return output;
	}
	exports.objectAssign = objectAssign;
	function copy(a, b) {
	    return objectAssign(objectAssign({}, a), b);
	}
	exports.copy = copy;
	function merge(target, source) {
	    if (target === undefined || target === null) {
	        throw new TypeError('Cannot convert undefined or null to object');
	    }
	    var output = Object(target);
	    if (source !== undefined && source !== null) {
	        for (var nextKey in source) {
	            if (source.hasOwnProperty(nextKey)) {
	                var v = source[nextKey];
	                if (v !== null || v !== undefined) {
	                    var oldv = output[nextKey];
	                    if (Array.isArray(v)) {
	                        if (Array.isArray(oldv)) {
	                            output[nextKey] = oldv.concat(v);
	                        }
	                        else {
	                            output[nextKey] = v;
	                        }
	                    }
	                    else if (typeof v === "object") {
	                        if (typeof oldv === "object") {
	                            output[nextKey] = merge(oldv, v);
	                        }
	                        else {
	                            output[nextKey] = v;
	                        }
	                    }
	                    else {
	                        output[nextKey] = v;
	                    }
	                }
	            }
	        }
	    }
	    return output;
	}
	exports.merge = merge;


/***/ },
/* 36 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var Util_1 = __webpack_require__(35);
	var Util = __webpack_require__(35);
	var flib_1 = __webpack_require__(5);
	var ddd_briscola_model_1 = __webpack_require__(15);
	var nrest_fetch_1 = __webpack_require__(19);
	function asGameEventAndState(a) {
	    if (a instanceof ddd_briscola_model_1.GameEventAndState)
	        return flib_1.Option.some(a);
	    else
	        return flib_1.Option.None;
	}
	function asCompetitionEventAndState(a) {
	    if (a instanceof ddd_briscola_model_1.CompetitionEventAndState)
	        return flib_1.Option.some(a);
	    else
	        return flib_1.Option.None;
	}
	function asPlayerEventAndState(a) {
	    if (a instanceof ddd_briscola_model_1.PlayerEventAndState)
	        return flib_1.Option.some(a);
	    else
	        return flib_1.Option.None;
	}
	var PlayerService = (function () {
	    function PlayerService(resourceFetch, player) {
	        this.resourceFetch = resourceFetch;
	        this.player = player;
	        var webSocket = Util_1.observableWebSocket(player.webSocket).flatMap(function (msgEv) {
	            var data = msgEv.data;
	            if (typeof data === "string") {
	                var msg = JSON.parse(data);
	                return resourceFetch.fetchObject(msg, ddd_briscola_model_1.eventAndStateChoice).then(function (v) { return Promise.resolve(v); }, function (err) {
	                    console.log("error parsing web socket message");
	                    console.log("error " + err);
	                    if (err && err.stack)
	                        console.log(err.stack);
	                    console.log(data);
	                    return Promise.reject("error fetching event and state " + data + ", error : " + err);
	                });
	            }
	            else {
	                console.log("Error");
	                console.log("msgEv.data:");
	                console.log(data);
	                return Promise.reject("expecting string");
	            }
	        });
	        this.gamesChannel = Util.rxCollect(webSocket, asGameEventAndState);
	        this.competitionsChannel = Util.rxCollect(webSocket, asCompetitionEventAndState);
	        this.playersChannel = Util.rxCollect(webSocket, asPlayerEventAndState);
	        this.eventsLog = webSocket.map(function (es) { return es.event; });
	    }
	    PlayerService.prototype.createCompetition = function (players, kind, deadlineKind) {
	        var _this = this;
	        return Util.Http.POST(this.player.createCompetition, {
	            players: players,
	            kind: kind,
	            deadline: deadlineKind
	        }).then(function (p) { return _this.resourceFetch.fetchObject(p, nrest_fetch_1.mapping(ddd_briscola_model_1.CompetitionState)); });
	    };
	    PlayerService.prototype.playCard = function (gameState, mcard) {
	        var _this = this;
	        var card = ddd_briscola_model_1.Input.card(mcard);
	        var url = gameState.playerState.map(function (ps) { return ps.self; });
	        return url.map(function (url) {
	            return Util.Http.POST(url, {
	                "number": card.number,
	                seed: card.seed
	            }).then(function (resp) {
	                return resp.json().then(function (ws) {
	                    return _this.resourceFetch.fetchObject(ws, ddd_briscola_model_1.gameStateChoice);
	                });
	            });
	        });
	    };
	    PlayerService.prototype.acceptCompetition = function (cs) {
	        var _this = this;
	        return cs.accept.map(function (url) {
	            return Util.Http.POST(url).then(function (resp) {
	                return resp.json().then(function (ws) { return _this.resourceFetch.fetchObject(ws, nrest_fetch_1.mapping(ddd_briscola_model_1.CompetitionState)); });
	            });
	        });
	    };
	    PlayerService.prototype.declineCompetition = function (cs) {
	        var _this = this;
	        return cs.decline.map(function (url) {
	            return Util.Http.POST(url).then(function (resp) {
	                return resp.json().then(function (ws) { return _this.resourceFetch.fetchObject(ws, nrest_fetch_1.mapping(ddd_briscola_model_1.CompetitionState)); });
	            });
	        });
	    };
	    return PlayerService;
	}());
	exports.PlayerService = PlayerService;


/***/ },
/* 37 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var Reducers = __webpack_require__(38);
	function applicationDispatch() {
	    return function (cmd) {
	        switch (cmd.type) {
	            case "startApplication": return Reducers.synchStateChange(function (st) { return st; });
	            case "createPlayer":
	            case "playerLogon": return Reducers.playerLogon(cmd);
	            case "playCard": return Reducers.playCard(cmd);
	            case "acceptCompetition":
	            case "declineCompetition":
	            case "startCompetition": return Reducers.competitionCommands(cmd);
	            case "diplayPlayerDeck": return Reducers.synchReducer(Reducers.diplayPlayerDeck)(cmd);
	            case "newDomainEvent": return Reducers.synchReducer(Reducers.newDomainEvent)(cmd);
	            case "selectPlayerForCompetition": return Reducers.synchReducer(Reducers.selectPlayerForCompetition)(cmd);
	            case "setCompetitionDeadline": return Reducers.synchReducer(Reducers.setCompetitionDeadline)(cmd);
	            case "setCompetitionKind": return Reducers.synchReducer(Reducers.setCompetitionKind)(cmd);
	            case "setCurrentGame": return Reducers.synchReducer(Reducers.setCurrentGame)(cmd);
	            case "updateCompetitionState": return Reducers.synchReducer(Reducers.updateCompetionState)(cmd);
	            case "updateGameState": return Reducers.synchReducer(Reducers.updateGameState)(cmd);
	            case "updatePlayersState": return Reducers.synchReducer(Reducers.updatePlayersState)(cmd);
	        }
	    };
	}
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = applicationDispatch;


/***/ },
/* 38 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var Commands = __webpack_require__(39);
	var ddd_briscola_model_1 = __webpack_require__(15);
	var Util_1 = __webpack_require__(35);
	function synchReducer(rt) {
	    return function (command) { return synchStateChange(rt(command)); };
	}
	exports.synchReducer = synchReducer;
	function synchStateChange(sc) {
	    return function (st, d) {
	        try {
	            var r = sc(st, d);
	            return Promise.resolve(r);
	        }
	        catch (e) {
	            return Promise.reject(e);
	        }
	    };
	}
	exports.synchStateChange = synchStateChange;
	exports.playerLogon = function (command) { return function (state, dispatch) {
	    var ps = state.playersService;
	    var createPlayer = (command instanceof Commands.PlayerLogon) ? ps.logon(command.playerName, command.password) : ps.createPlayer(command.playerName, command.password);
	    return createPlayer.then(function (player) {
	        var playerService = state.createPlayerService(player);
	        playerService.eventsLog.subscribe(function (event) {
	            dispatch(new Commands.NewDomainEvent(event));
	        });
	        playerService.gamesChannel.subscribe(function (es) {
	            dispatch(new Commands.UpdateGameState(es.game));
	        });
	        playerService.competitionsChannel.subscribe(function (es) {
	            dispatch(new Commands.UpdateCompetitionState(es.competition));
	        });
	        playerService.playersChannel.subscribe(function (es) {
	            dispatch(new Commands.UpdatePlayersState(es.players));
	        });
	        var board = Util_1.copy(state.board, {
	            player: flib_1.Option.some(player)
	        });
	        var createPlayerService = state.createPlayerService, playersService = state.playersService;
	        return {
	            playersService: playersService,
	            playerService: flib_1.Option.some(playerService),
	            board: board,
	            createPlayerService: createPlayerService
	        };
	    });
	}; };
	exports.playCard = function (command) { return function (state) {
	    return state.playerService.map(function (playerService) {
	        var game = state.board.currentGame.fold(function () { return Promise.reject(new Error("no current game")); }, function (g) { return (g instanceof ddd_briscola_model_1.ActiveGameState) ? Promise.resolve(g) : Promise.reject(new Error("game is not active")); });
	        return game.then(function (game) {
	            playerService.playCard(game, command.card);
	            var createPlayerService = state.createPlayerService, playersService = state.playersService, board = state.board;
	            var nst = {
	                playersService: playersService,
	                playerService: flib_1.Option.some(playerService),
	                board: board,
	                createPlayerService: createPlayerService,
	            };
	            return nst;
	        });
	    }).getOrElse(function () { return Promise.reject("player service not avaiable"); });
	}; };
	exports.competitionCommands = function (command) { return function (state, dispacth) {
	    return state.playerService.map(function (playerService) {
	        if (command instanceof Commands.StartCompetition) {
	            var resp = playerService.createCompetition(Object.keys(state.board.competitionSelectedPlayers), state.board.competitionKind, state.board.competitionDeadlineKind);
	            return resp.then(function (r) { return state; });
	        }
	        else {
	            var competitionState = state.board.engagedCompetitions[command.competition];
	            if (competitionState != null && competitionState != undefined) {
	                if (command instanceof Commands.AcceptCompetition) {
	                    return playerService.acceptCompetition(competitionState).fold(function () { return Promise.reject("could not accept the competition"); }, function () { return Promise.resolve(state); });
	                }
	                else {
	                    return playerService.declineCompetition(competitionState).fold(function () { return Promise.reject("could not reject the competition"); }, function () { return Promise.resolve(state); });
	                }
	            }
	            else
	                return Promise.reject(new Error("Invalid competition"));
	        }
	    }).getOrElse(function () { return Promise.reject(new Error("Player service is unavaiable")); });
	}; };
	function boardReducer(br) {
	    return function (command) { return function (state) {
	        var createPlayerService = state.createPlayerService, playersService = state.playersService, playerService = state.playerService;
	        return {
	            playersService: playersService,
	            playerService: playerService,
	            board: br(command)(state.board),
	            createPlayerService: createPlayerService
	        };
	    }; };
	}
	exports.selectPlayerForCompetition = boardReducer(function (command) { return function (board) {
	    if (command.selected) {
	        return Util_1.copy(board, {
	            competitionSelectedPlayers: flib_1.JsMap.merge([board.competitionSelectedPlayers, (_a = {},
	                    _a[command.player] = true,
	                    _a
	                )])
	        });
	    }
	    else {
	        var cpy = Util_1.copy(board, {});
	        delete cpy.competitionSelectedPlayers[command.player];
	        return cpy;
	    }
	    var _a;
	}; });
	exports.setCompetitionKind = boardReducer(function (command) { return function (board) {
	    return Util_1.copy(board, {
	        competitionKind: command.kind
	    });
	}; });
	exports.setCompetitionDeadline = boardReducer(function (command) { return function (board) {
	    return Util_1.copy(board, {
	        competitionDeadlineKind: command.deadlineKind
	    });
	}; });
	exports.setCurrentGame = boardReducer(function (command) { return function (board) {
	    return Util_1.copy(board, {
	        currentGame: flib_1.Option.option(board.activeGames[command.game]).orElse(function () { return flib_1.Option.option(board.finishedGames[command.game]); })
	    });
	}; });
	exports.diplayPlayerDeck = boardReducer(function (command) { return function (board) {
	    return Util_1.copy(board, {
	        viewFlag: command.display === true ? ddd_briscola_model_1.ViewFlag.showPlayerCards : ddd_briscola_model_1.ViewFlag.normal
	    });
	}; });
	exports.updatePlayersState = boardReducer(function (command) { return function (board) {
	    return Util_1.copy(board, {
	        players: board.player.map(function (cp) { return command.players.filter(function (pl) { return pl.self !== cp.self; }); }).getOrElse(function () { return command.players; })
	    });
	}; });
	exports.updateGameState = boardReducer(function (command) { return function (board) {
	    var gm = command.gameState;
	    var res = board;
	    var currentGame = board.currentGame.fold(function () { return flib_1.Option.some(gm); }, function (cgm) {
	        if (gm.self === cgm.self) {
	            return flib_1.Option.some(gm);
	        }
	        else {
	            return flib_1.Option.some(cgm);
	        }
	    });
	    if (gm instanceof ddd_briscola_model_1.ActiveGameState) {
	        res = Util_1.copy(board, {
	            currentGame: currentGame,
	            activeGames: flib_1.JsMap.merge([board.activeGames, (_a = {},
	                    _a[gm.self] = gm,
	                    _a
	                )])
	        });
	    }
	    else if (gm instanceof ddd_briscola_model_1.FinalGameState) {
	        res = Util_1.copy(board, {
	            currentGame: currentGame,
	            finishedGames: flib_1.JsMap.merge([board.finishedGames, (_b = {},
	                    _b[gm.self] = gm,
	                    _b
	                )])
	        });
	        delete res.activeGames[gm.self];
	    }
	    return res;
	    var _a, _b;
	}; });
	exports.updateCompetionState = boardReducer(function (command) { return function (board) {
	    return Util_1.copy(board, {
	        engagedCompetitions: flib_1.JsMap.merge([board.engagedCompetitions, (_a = {},
	                _a[command.competitionState.self] = command.competitionState,
	                _a
	            )])
	    });
	    var _a;
	}; });
	exports.newDomainEvent = boardReducer(function (command) { return function (board) {
	    return Util_1.copy(board, {
	        eventsLog: [command.event].concat(board.eventsLog)
	    });
	}; });


/***/ },
/* 39 */
/***/ function(module, exports) {

	"use strict";
	var StarApplication = (function () {
	    function StarApplication() {
	        this.type = "startApplication";
	    }
	    return StarApplication;
	}());
	exports.StarApplication = StarApplication;
	var CreatePlayer = (function () {
	    function CreatePlayer(playerName, password) {
	        this.playerName = playerName;
	        this.password = password;
	        this.type = "createPlayer";
	    }
	    return CreatePlayer;
	}());
	exports.CreatePlayer = CreatePlayer;
	var PlayerLogon = (function () {
	    function PlayerLogon(playerName, password) {
	        this.playerName = playerName;
	        this.password = password;
	        this.type = "playerLogon";
	    }
	    return PlayerLogon;
	}());
	exports.PlayerLogon = PlayerLogon;
	var StartCompetition = (function () {
	    function StartCompetition() {
	        this.type = "startCompetition";
	    }
	    return StartCompetition;
	}());
	exports.StartCompetition = StartCompetition;
	var AcceptCompetition = (function () {
	    function AcceptCompetition(competition) {
	        this.competition = competition;
	        this.type = "acceptCompetition";
	    }
	    return AcceptCompetition;
	}());
	exports.AcceptCompetition = AcceptCompetition;
	var DeclineCompetition = (function () {
	    function DeclineCompetition(competition) {
	        this.competition = competition;
	        this.type = "declineCompetition";
	    }
	    return DeclineCompetition;
	}());
	exports.DeclineCompetition = DeclineCompetition;
	var PlayCard = (function () {
	    function PlayCard(card) {
	        this.card = card;
	        this.type = "playCard";
	    }
	    return PlayCard;
	}());
	exports.PlayCard = PlayCard;
	var SelectPlayerForCompetition = (function () {
	    function SelectPlayerForCompetition(player, selected) {
	        this.player = player;
	        this.selected = selected;
	        this.type = "selectPlayerForCompetition";
	    }
	    return SelectPlayerForCompetition;
	}());
	exports.SelectPlayerForCompetition = SelectPlayerForCompetition;
	var SetCompetitionKind = (function () {
	    function SetCompetitionKind(kind) {
	        this.kind = kind;
	        this.type = "setCompetitionKind";
	    }
	    return SetCompetitionKind;
	}());
	exports.SetCompetitionKind = SetCompetitionKind;
	var SetCompetitionDeadline = (function () {
	    function SetCompetitionDeadline(deadlineKind) {
	        this.deadlineKind = deadlineKind;
	        this.type = "setCompetitionDeadline";
	    }
	    return SetCompetitionDeadline;
	}());
	exports.SetCompetitionDeadline = SetCompetitionDeadline;
	var SetCurrentGame = (function () {
	    function SetCurrentGame(game) {
	        this.game = game;
	        this.type = "setCurrentGame";
	    }
	    return SetCurrentGame;
	}());
	exports.SetCurrentGame = SetCurrentGame;
	var DiplayPlayerDeck = (function () {
	    function DiplayPlayerDeck(game, display) {
	        this.game = game;
	        this.display = display;
	        this.type = "diplayPlayerDeck";
	    }
	    return DiplayPlayerDeck;
	}());
	exports.DiplayPlayerDeck = DiplayPlayerDeck;
	var UpdateGameState = (function () {
	    function UpdateGameState(gameState) {
	        this.gameState = gameState;
	        this.type = "updateGameState";
	    }
	    return UpdateGameState;
	}());
	exports.UpdateGameState = UpdateGameState;
	var UpdatePlayersState = (function () {
	    function UpdatePlayersState(players) {
	        this.players = players;
	        this.type = "updatePlayersState";
	    }
	    return UpdatePlayersState;
	}());
	exports.UpdatePlayersState = UpdatePlayersState;
	var UpdateCompetitionState = (function () {
	    function UpdateCompetitionState(competitionState) {
	        this.competitionState = competitionState;
	        this.type = "updateCompetitionState";
	    }
	    return UpdateCompetitionState;
	}());
	exports.UpdateCompetitionState = UpdateCompetitionState;
	var NewDomainEvent = (function () {
	    function NewDomainEvent(event) {
	        this.event = event;
	        this.type = "newDomainEvent";
	    }
	    return NewDomainEvent;
	}());
	exports.NewDomainEvent = NewDomainEvent;


/***/ },
/* 40 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var ddd_briscola_app_1 = __webpack_require__(1);
	function createBoardCommandListener(board, commandListener) {
	    return {
	        board: board,
	        onStartCompetition: function () {
	            commandListener(new ddd_briscola_app_1.Commands.StartCompetition());
	        },
	        onCreatePlayer: function (name, password) {
	            commandListener(new ddd_briscola_app_1.Commands.CreatePlayer(name, password));
	        },
	        onPlayerLogin: function (name, password) {
	            commandListener(new ddd_briscola_app_1.Commands.PlayerLogon(name, password));
	        },
	        onPlayerSelection: function (player, selected) {
	            commandListener(new ddd_briscola_app_1.Commands.SelectPlayerForCompetition(player.self, selected));
	        },
	        onAcceptCompetition: function (cid) {
	            commandListener(new ddd_briscola_app_1.Commands.AcceptCompetition(cid.self));
	        },
	        onDeclineCompetition: function (cid) {
	            commandListener(new ddd_briscola_app_1.Commands.DeclineCompetition(cid.self));
	        },
	        onSelectedGame: function (gm) {
	            commandListener(new ddd_briscola_app_1.Commands.SetCurrentGame(gm));
	        },
	        onPlayCard: function (c) {
	            commandListener(new ddd_briscola_app_1.Commands.PlayCard(c));
	        },
	        onPlayerDeck: function (game, display) {
	            commandListener(new ddd_briscola_app_1.Commands.DiplayPlayerDeck(game, display));
	        }
	    };
	}
	exports.createBoardCommandListener = createBoardCommandListener;


/***/ },
/* 41 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var __assign = (this && this.__assign) || Object.assign || function(t) {
	    for (var s, i = 1, n = arguments.length; i < n; i++) {
	        s = arguments[i];
	        for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
	            t[p] = s[p];
	    }
	    return t;
	};
	var flib_1 = __webpack_require__(5);
	var Model = __webpack_require__(15);
	var cssClasses_1 = __webpack_require__(42);
	var cards_1 = __webpack_require__(43);
	var EventsLog_1 = __webpack_require__(44);
	var GameResult_1 = __webpack_require__(45);
	var PlayerDeck_1 = __webpack_require__(47);
	var PlayerLogin_1 = __webpack_require__(48);
	var Players_1 = __webpack_require__(49);
	var StartCompetition = (function (_super) {
	    __extends(StartCompetition, _super);
	    function StartCompetition() {
	        _super.call(this);
	    }
	    StartCompetition.prototype.render = function () {
	        var props = this.props;
	        return (React.createElement("input", {type: "button", onClick: function (e) { return props.onStartCompetition(); }, value: "Create Competition"}));
	    };
	    return StartCompetition;
	}(React.Component));
	var GameTable = (function (_super) {
	    __extends(GameTable, _super);
	    function GameTable() {
	        _super.apply(this, arguments);
	    }
	    GameTable.prototype.render = function () {
	        var gm = this.props.game;
	        var elemEvs = gm.moves.map(function (mv, idx) {
	            return React.createElement("div", {key: idx}, 
	                React.createElement(cards_1.Card, {card: mv.card}), 
	                React.createElement("span", null, mv.player.name));
	        });
	        elemEvs = elemEvs.concat(gm.nextPlayers.map(function (p, idx) {
	            return React.createElement("div", {key: idx + gm.moves.length}, 
	                React.createElement(cards_1.EmptyCard, null), 
	                React.createElement("span", null, p.name));
	        }));
	        elemEvs.push(React.createElement("div", {key: gm.nextPlayers.length + gm.moves.length + 1}, 
	            React.createElement(cards_1.Card, {card: gm.briscolaCard, classes: [cssClasses_1.default.halfCard]}), 
	            React.createElement(cards_1.CardBack, {classes: [cssClasses_1.default.halfCard]}), 
	            React.createElement("span", null, gm.deckCardsNumber)));
	        return (React.createElement("div", {className: cssClasses_1.default.cards}, elemEvs));
	    };
	    return GameTable;
	}(React.Component));
	exports.GameTable = GameTable;
	function showCompetitionButton(board) {
	    var len = Object.keys(board.competitionSelectedPlayers).length + 1;
	    return (len >= board.config.minPlayersNumber) && (len <= board.config.maxPlayersNumber);
	}
	var PlayerCards = (function (_super) {
	    __extends(PlayerCards, _super);
	    function PlayerCards() {
	        _super.apply(this, arguments);
	    }
	    PlayerCards.prototype.render = function () {
	        var props = this.props;
	        var gameId = props.game.self;
	        var playerState = props.game.playerState;
	        var elems = playerState.map(function (ps) {
	            return ps.cards.map(function (card, idx) { return React.createElement("div", {key: idx}, 
	                React.createElement(cards_1.Card, {card: card, onClick: function () { return props.onPlayCard(card); }})
	            ); });
	        }).getOrElse(function () { return [React.createElement("span", {key: "0"}, "Player has no cards")]; });
	        return (React.createElement("div", {className: cssClasses_1.default.cards}, 
	            elems, 
	            React.createElement("div", null, 
	                React.createElement(cards_1.CardBack, {onClick: function () { return props.onPlayerDeck(gameId, true); }})
	            )));
	    };
	    return PlayerCards;
	}(React.Component));
	exports.PlayerCards = PlayerCards;
	var Game = (function (_super) {
	    __extends(Game, _super);
	    function Game() {
	        _super.apply(this, arguments);
	    }
	    Game.prototype.render = function () {
	        var props = this.props;
	        var gm = props.game;
	        return Model.GameState.fold(gm, function (gm) {
	            return React.createElement("section", null, 
	                React.createElement(GameTable, {game: gm}), 
	                React.createElement(PlayerCards, {game: gm, onPlayerDeck: props.onPlayerDeck, onPlayCard: props.onPlayCard}));
	        }, function (gm) {
	            return React.createElement("section", null, 
	                React.createElement(GameResult_1.GameResult, {game: gm})
	            );
	        }, function (gm) {
	            return React.createElement("section", null, 
	                React.createElement("h1", null, "Dropped Game !!!")
	            );
	        });
	    };
	    return Game;
	}(React.Component));
	var GamesNav = (function (_super) {
	    __extends(GamesNav, _super);
	    function GamesNav() {
	        _super.apply(this, arguments);
	    }
	    GamesNav.prototype.render = function () {
	        var props = this.props;
	        var games = props.games;
	        var clss = ([cssClasses_1.default.gamesNav].concat(flib_1.isNull(props.classes) ? [] : props.classes)).join(" ");
	        return games.length == 0 ?
	            React.createElement("noscript", null) :
	            React.createElement("div", {className: clss}, games.map(function (gm, idx) {
	                var selectedClassName = props.current === gm ? cssClasses_1.default.selected : "";
	                return React.createElement("span", {className: selectedClassName, onClick: function (ev) { return props.onSelectedGame(gm); }, key: gm}, idx);
	            }));
	    };
	    return GamesNav;
	}(React.Component));
	var Board = (function (_super) {
	    __extends(Board, _super);
	    function Board() {
	        _super.apply(this, arguments);
	    }
	    Board.prototype.render = function () {
	        var props = this.props;
	        var board = this.props.board;
	        var gameSection = board.currentGame.map(function (gm) {
	            return React.createElement("section", null, 
	                React.createElement(Game, {game: gm, onPlayerDeck: props.onPlayerDeck, onPlayCard: props.onPlayCard}), 
	                React.createElement(GamesNav, {current: gm.self, games: Object.keys(board.activeGames), onSelectedGame: props.onSelectedGame}), 
	                React.createElement(GamesNav, {current: gm.self, games: Object.keys(board.finishedGames), onSelectedGame: props.onSelectedGame}));
	        }).getOrElse(function () { return React.createElement("noscript", null); });
	        var playerDeckDialog = board.viewFlag === Model.ViewFlag.showPlayerCards ?
	            board.currentGame.map(function (gm) {
	                return Model.GameState.fold(gm, function (gm) { return React.createElement(PlayerDeck_1.PlayerDeckSummary, {cards: gm.playerState.map(function (ps) { return ps.score.cards; }).getOrElse(function () { return []; }), onClose: function () { return props.onPlayerDeck(gm.self, false); }}); }, function (gm) { return React.createElement("noscript", null); }, function (gm) { return React.createElement("noscript", null); });
	            }).getOrElse(function () { return React.createElement("noscript", null); }) : React.createElement("noscript", null);
	        var startCompetitionButton = showCompetitionButton(board) ? React.createElement(StartCompetition, {onStartCompetition: props.onStartCompetition}) : React.createElement("noscript", null);
	        return board.player.map(function (pl) { return (React.createElement("div", null, 
	            playerDeckDialog, 
	            React.createElement(Players_1.CurrentPlayer, __assign({}, pl)), 
	            React.createElement(Players_1.Players, {players: board.players, selectedPlayers: board.competitionSelectedPlayers, onPlayerSelection: props.onPlayerSelection}), 
	            startCompetitionButton, 
	            gameSection, 
	            React.createElement(EventsLog_1.EventsLog, {events: board.eventsLog, onAcceptCompetition: props.onAcceptCompetition, onDeclineCompetition: props.onDeclineCompetition, onSelectedGame: props.onSelectedGame}))); }).getOrElse(function () { return (React.createElement(PlayerLogin_1.PlayerLogin, {onCreatePlayer: props.onCreatePlayer, onPlayerLogin: props.onPlayerLogin})); });
	    };
	    return Board;
	}(React.Component));
	exports.Board = Board;


/***/ },
/* 42 */
/***/ function(module, exports) {

	"use strict";
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = {
	    card: "card",
	    emptyCard: "card-empty",
	    cardBack: "card-back",
	    halfCard: "card-half",
	    cards: "cards",
	    currentPlayer: "current-player",
	    players: "players",
	    createPlayer: "create-player",
	    eventLog: "event-log",
	    playerDeckLayer: "player-deck-layer",
	    playerDeckSummary: "player-deck-summary",
	    gamesNav: "games-nav",
	    selected: "selected"
	};


/***/ },
/* 43 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var __assign = (this && this.__assign) || Object.assign || function(t) {
	    for (var s, i = 1, n = arguments.length; i < n; i++) {
	        s = arguments[i];
	        for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
	            t[p] = s[p];
	    }
	    return t;
	};
	var Model = __webpack_require__(15);
	var cssClasses_1 = __webpack_require__(42);
	var flib_1 = __webpack_require__(5);
	var Card = (function (_super) {
	    __extends(Card, _super);
	    function Card() {
	        _super.apply(this, arguments);
	    }
	    Card.prototype.render = function () {
	        var props = this.props;
	        var card = props.card;
	        var clss = flib_1.isNull(props.classes) ? "" : props.classes.join(" ");
	        var className = cssClasses_1.default.card + " " + clss + " card_" + Model.Seed[card.seed] + "_" + card.number;
	        var extra = flib_1.isNull(props.key) ? {} : { key: props.key };
	        var onClick = function (ev) {
	            console.log("triggered card onClick");
	            props.onClick && props.onClick();
	        };
	        return (React.createElement("img", __assign({}, extra, {className: className, onClick: onClick})));
	    };
	    return Card;
	}(React.Component));
	exports.Card = Card;
	var EmptyCard = (function (_super) {
	    __extends(EmptyCard, _super);
	    function EmptyCard() {
	        _super.apply(this, arguments);
	    }
	    EmptyCard.prototype.render = function () {
	        var props = this.props;
	        var clss = flib_1.isNull(props.classes) ? "" : props.classes.join(" ");
	        return (React.createElement("img", {className: cssClasses_1.default.card + " " + clss + " " + cssClasses_1.default.emptyCard, onClick: function (ev) { return !flib_1.isNull(props.onClick) ? props.onClick() : undefined; }}));
	    };
	    return EmptyCard;
	}(React.Component));
	exports.EmptyCard = EmptyCard;
	var CardBack = (function (_super) {
	    __extends(CardBack, _super);
	    function CardBack() {
	        _super.apply(this, arguments);
	    }
	    CardBack.prototype.render = function () {
	        var props = this.props;
	        var clss = flib_1.isNull(props.classes) ? "" : props.classes.join(" ");
	        return (React.createElement("img", {className: cssClasses_1.default.card + " " + clss + " " + cssClasses_1.default.cardBack, onClick: function (ev) { return !flib_1.isNull(props.onClick) ? props.onClick() : undefined; }}));
	    };
	    return CardBack;
	}(React.Component));
	exports.CardBack = CardBack;


/***/ },
/* 44 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var Model = __webpack_require__(15);
	var PlayerEventKind = Model.PlayerEventKind;
	var BriscolaEventKind = Model.BriscolaEventKind;
	var CompetitionEventKind = Model.CompetitionEventKind;
	var cssClasses_1 = __webpack_require__(42);
	var EventsLog = (function (_super) {
	    __extends(EventsLog, _super);
	    function EventsLog() {
	        _super.apply(this, arguments);
	    }
	    EventsLog.prototype.render = function () {
	        var props = this.props;
	        var events = props.events;
	        var elemEvs = events.map(function (event, i) {
	            switch (event.eventName) {
	                case PlayerEventKind[PlayerEventKind.playerLogOn]: {
	                    var ev = event;
	                    return (React.createElement("div", {key: i}, 
	                        "Player ", 
	                        React.createElement("b", null, ev.player.name), 
	                        " log on"));
	                }
	                case PlayerEventKind[PlayerEventKind.playerLogOff]: {
	                    var ev = event;
	                    return (React.createElement("div", {key: i}, 
	                        "Player ", 
	                        React.createElement("b", null, ev.player.name), 
	                        " log off"));
	                }
	                case CompetitionEventKind[CompetitionEventKind.createdCompetition]: {
	                    var ev_1 = event;
	                    return (React.createElement("div", {key: i}, 
	                        React.createElement("p", null, 
	                            "Player ", 
	                            ev_1.issuer.name, 
	                            " invited you !!!"), 
	                        React.createElement("p", null, 
	                            "players are ", 
	                            ev_1.competition.competition.players.map(function (pl) { return React.createElement("b", null, 
	                                pl.name, 
	                                " "); }), 
	                            " of kind ", 
	                            React.createElement("b", null, Model.MatchKindKind[ev_1.competition.competition.kind.kind]), 
	                            " "), 
	                        React.createElement("p", null, 
	                            ev_1.competition.accept.map(function (url) {
	                                return React.createElement("button", {onClick: function (clkEv) { return props.onAcceptCompetition(ev_1.competition); }, key: "1"}, "Accept competiton");
	                            }).getOrElse(function () { return React.createElement("noscript", null); }), 
	                            ev_1.competition.decline.map(function (url) {
	                                return React.createElement("button", {onClick: function (clkEv) { return props.onDeclineCompetition(ev_1.competition); }, key: "1"}, "Decline competiton");
	                            }).getOrElse(function () { return React.createElement("noscript", null); }))));
	                }
	                case BriscolaEventKind[BriscolaEventKind.gameStarted]: {
	                    var ev_2 = event;
	                    return (React.createElement("div", {key: i}, 
	                        React.createElement("a", {href: "#", onClick: function (cev) { return props.onSelectedGame(ev_2.game.self); }}, "A game has started")
	                    ));
	                }
	                case BriscolaEventKind[BriscolaEventKind.cardPlayed]: {
	                    var ev_3 = event;
	                    return (React.createElement("div", {key: i}, 
	                        React.createElement("a", {href: "#", onClick: function (cev) { return props.onSelectedGame(ev_3.game.self); }}, 
	                            ev_3.player.name, 
	                            " played ", 
	                            ev_3.card.number, 
	                            " ", 
	                            Model.Seed[ev_3.card.seed])
	                    ));
	                }
	                default: {
	                    return (React.createElement("div", {key: i}, 
	                        React.createElement("textarea", {rows: 4, cols: 80, defaultValue: JSON.stringify(event)})
	                    ));
	                }
	            }
	        });
	        return (React.createElement("div", {className: cssClasses_1.default.eventLog}, elemEvs));
	    };
	    return EventsLog;
	}(React.Component));
	exports.EventsLog = EventsLog;


/***/ },
/* 45 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var Model = __webpack_require__(15);
	var cssClasses_1 = __webpack_require__(42);
	var TabPane_1 = __webpack_require__(46);
	var PlayerDeck_1 = __webpack_require__(47);
	function playersGameResultTabPaneItems(gameRes) {
	    return gameRes.playersOrderByPoints.map(function (pl) {
	        var r = {
	            activator: function (selected, selectItem, idx) {
	                return React.createElement("span", {key: idx}, 
	                    React.createElement("button", {onClick: function (ev) { return selectItem(); }}, pl.player.name)
	                );
	            },
	            content: function () {
	                return (React.createElement("div", null, 
	                    React.createElement(PlayerDeck_1.PlayerDeckSummaryCards, {cards: pl.score.cards}), 
	                    React.createElement("div", {className: cssClasses_1.default.playerDeckSummary}, 
	                        React.createElement("span", null, pl.points)
	                    )));
	            }
	        };
	        return r;
	    });
	}
	function teamGameResultTabPaneItems(gameRes) {
	    return gameRes.teamsOrderByPoints.map(function (teamScore) {
	        var total = teamScore.points;
	        var r = {
	            activator: function (selected, selectItem, idx) {
	                return React.createElement("span", {key: idx}, 
	                    React.createElement("button", {onClick: function (ev) { return selectItem(); }}, 
	                        teamScore.teamName, 
	                        " : ", 
	                        teamScore.players.map(function (pl) { return pl.name; }).join(", "))
	                );
	            },
	            content: function () {
	                return (React.createElement("div", null, 
	                    React.createElement(PlayerDeck_1.PlayerDeckSummaryCards, {cards: teamScore.cards}), 
	                    React.createElement("div", {className: cssClasses_1.default.playerDeckSummary}, 
	                        React.createElement("span", null, total)
	                    )));
	            }
	        };
	        return r;
	    });
	}
	function playersResultsTabPaneItems(gm) {
	    var gmRes = gm.gameResult;
	    if (gmRes instanceof Model.TeamsGameResult)
	        return teamGameResultTabPaneItems(gmRes);
	    else if (gmRes instanceof Model.PlayersGameResult)
	        return playersGameResultTabPaneItems(gmRes);
	    throw new Error("unexcepted " + gm);
	}
	var GameResult = (function (_super) {
	    __extends(GameResult, _super);
	    function GameResult() {
	        _super.apply(this, arguments);
	    }
	    GameResult.prototype.render = function () {
	        var props = this.props;
	        var gm = props.game;
	        return React.createElement(TabPane_1.TabPane, {panes: playersResultsTabPaneItems(gm)});
	    };
	    return GameResult;
	}(React.Component));
	exports.GameResult = GameResult;


/***/ },
/* 46 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var flib_1 = __webpack_require__(5);
	var TabPane = (function (_super) {
	    __extends(TabPane, _super);
	    function TabPane() {
	        _super.apply(this, arguments);
	        this.state = {
	            current: 0
	        };
	    }
	    TabPane.prototype.render = function () {
	        var _this = this;
	        var props = this.props;
	        var currIdx = this.state.current;
	        var curr = props.panes[currIdx];
	        var mainArea = flib_1.isNull(curr) ? (React.createElement("div", null, "missing pane")) : (curr.content(currIdx));
	        var activators = props.panes.map(function (pane, idx) {
	            return pane.activator((idx === currIdx), function () { _this.setState({ current: idx }); }, idx);
	        });
	        return (React.createElement("div", {className: props.classes && props.classes.container || ""}, 
	            React.createElement("div", {className: props.classes && props.classes.mainArea || ""}, mainArea), 
	            React.createElement("div", {className: props.classes && props.classes.activatorsContainer || ""}, activators)));
	    };
	    return TabPane;
	}(React.Component));
	exports.TabPane = TabPane;


/***/ },
/* 47 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var flib_1 = __webpack_require__(5);
	var Model = __webpack_require__(15);
	var cards_1 = __webpack_require__(43);
	var cssClasses_1 = __webpack_require__(42);
	var PlayerDeckSummaryCards = (function (_super) {
	    __extends(PlayerDeckSummaryCards, _super);
	    function PlayerDeckSummaryCards() {
	        _super.apply(this, arguments);
	    }
	    PlayerDeckSummaryCards.prototype.render = function () {
	        var cards = this.props.cards;
	        var cardsBySeed = flib_1.Arrays.groupBy(cards, function (c) { return Model.Seed[c.seed]; });
	        var items = Object.keys(cardsBySeed).map(function (seed, idx) {
	            var cards = cardsBySeed[seed];
	            cards.sort(function (ca, cb) {
	                var r = cb.points - ca.points;
	                return (r !== 0) ? r : (cb.number - ca.number);
	            });
	            var total = flib_1.Arrays.foldLeft(cards, 0, function (acc, card) { return acc + card.points; });
	            return (React.createElement("div", {key: idx, className: cssClasses_1.default.playerDeckSummary}, 
	                React.createElement("span", null, total), 
	                cards.map(function (card, idx) { return React.createElement(cards_1.Card, {key: idx, card: card}); })));
	        });
	        return React.createElement("div", null, items);
	    };
	    return PlayerDeckSummaryCards;
	}(React.Component));
	exports.PlayerDeckSummaryCards = PlayerDeckSummaryCards;
	var PlayerDeckSummary = (function (_super) {
	    __extends(PlayerDeckSummary, _super);
	    function PlayerDeckSummary() {
	        _super.apply(this, arguments);
	    }
	    PlayerDeckSummary.prototype.render = function () {
	        var props = this.props;
	        var total = flib_1.Arrays.foldLeft(props.cards, 0, function (acc, card) { return acc + card.points; });
	        return (React.createElement("div", null, 
	            React.createElement("div", {className: cssClasses_1.default.playerDeckLayer}, 
	                React.createElement(PlayerDeckSummaryCards, {cards: props.cards}), 
	                React.createElement("div", null, 
	                    React.createElement("span", null, total)
	                ), 
	                React.createElement("div", null, 
	                    React.createElement("button", {onClick: props.onClose}, "Close")
	                ))
	        ));
	    };
	    return PlayerDeckSummary;
	}(React.Component));
	exports.PlayerDeckSummary = PlayerDeckSummary;


/***/ },
/* 48 */
/***/ function(module, exports) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var PlayerLogin = (function (_super) {
	    __extends(PlayerLogin, _super);
	    function PlayerLogin() {
	        _super.call(this);
	    }
	    PlayerLogin.prototype.inputRef = function (nm) {
	        return this.refs[nm];
	    };
	    PlayerLogin.prototype.playerName = function () {
	        return this.inputRef(PlayerLogin.playerNameRef);
	    };
	    PlayerLogin.prototype.playerPassword = function () {
	        return this.inputRef(PlayerLogin.playerPasswordRef);
	    };
	    PlayerLogin.prototype.render = function () {
	        var _this = this;
	        var props = this.props;
	        return (React.createElement("div", {className: "{cssClasses.createPlayer} my-exp-style"}, 
	            React.createElement("input", {ref: PlayerLogin.playerNameRef, type: "text"}), 
	            React.createElement("input", {ref: PlayerLogin.playerPasswordRef, type: "password"}), 
	            React.createElement("input", {type: "button", onClick: function (e) { return props.onCreatePlayer(_this.playerName().value, _this.playerPassword().value); }, value: "Create Player"}), 
	            React.createElement("input", {type: "button", onClick: function (e) { return props.onPlayerLogin(_this.playerName().value, _this.playerPassword().value); }, value: "Log in"})));
	    };
	    PlayerLogin.playerNameRef = "playerName";
	    PlayerLogin.playerPasswordRef = "playerPasswordRef";
	    return PlayerLogin;
	}(React.Component));
	exports.PlayerLogin = PlayerLogin;


/***/ },
/* 49 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var cssClasses_1 = __webpack_require__(42);
	var CurrentPlayer = (function (_super) {
	    __extends(CurrentPlayer, _super);
	    function CurrentPlayer() {
	        _super.apply(this, arguments);
	    }
	    CurrentPlayer.prototype.render = function () {
	        var player = this.props;
	        return (React.createElement("div", {className: cssClasses_1.default.currentPlayer}, 
	            React.createElement("h5", null, "Current player: "), 
	            React.createElement("h4", null, player.name)));
	    };
	    return CurrentPlayer;
	}(React.Component));
	exports.CurrentPlayer = CurrentPlayer;
	var Players = (function (_super) {
	    __extends(Players, _super);
	    function Players() {
	        _super.apply(this, arguments);
	    }
	    Players.prototype.render = function () {
	        var _this = this;
	        var players = this.props.players;
	        var selectedPlayers = this.props.selectedPlayers;
	        var playersElems = players.map(function (pl, idx) {
	            return React.createElement("div", {key: idx}, 
	                React.createElement("input", {type: "checkbox", onChange: function (e) { return _this.props.onPlayerSelection(pl, !(selectedPlayers[pl.self] === true)); }, defaultChecked: selectedPlayers[pl.self]}), 
	                React.createElement("span", null, pl.name));
	        });
	        return (React.createElement("div", {className: cssClasses_1.default.players}, playersElems));
	    };
	    return Players;
	}(React.Component));
	exports.Players = Players;


/***/ },
/* 50 */
/***/ function(module, exports) {

	// removed by extract-text-webpack-plugin

/***/ }
/******/ ]);
//# sourceMappingURL=ddd-briscola-view.browser.js.map