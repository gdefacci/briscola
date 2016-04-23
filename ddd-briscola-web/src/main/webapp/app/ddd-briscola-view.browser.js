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
	var ddd_briscola_app_1 = __webpack_require__(1);
	var listeners_1 = __webpack_require__(44);
	var Board_1 = __webpack_require__(45);
	__webpack_require__(54);
	var app;
	var reactContainer = "react-container";
	function main() {
	    app = ddd_briscola_app_1.App.create("site-map");
	    var el = document.getElementById(reactContainer);
	    app.displayChannel.subscribe(function (board) {
	        var boardProps = listeners_1.createBoardCommandListener(board, function (cmd) { return app.exec(cmd); });
	        console.log("boardProps ");
	        console.log(boardProps);
	        ReactDOM.render(React.createElement(Board_1.Board, React.__spread({}, boardProps)), el);
	    });
	    app.exec(new ddd_briscola_app_1.Commands.StarApplication());
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
	// export * from "./lib/PlayersService"
	// export * from "./lib/PlayerService"
	__export(__webpack_require__(2));
	var Commands = __webpack_require__(12);
	exports.Commands = Commands;


/***/ },
/* 2 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var CommandID_1 = __webpack_require__(3);
	var Reducers = __webpack_require__(4);
	var CommandsDispatcher_1 = __webpack_require__(43);
	var App;
	(function (App) {
	    function create(entryPoint) {
	        return new AppImpl(entryPoint);
	    }
	    App.create = create;
	})(App = exports.App || (exports.App = {}));
	var AppImpl = (function () {
	    function AppImpl(entryPoint) {
	        var initialState = Reducers.initialState(entryPoint);
	        this.dispatcher = initialState.then(function (state) {
	            return new CommandsDispatcher_1.default(AppImpl.dispatch, state);
	        });
	        this.displayChannel = Rx.Observable.fromPromise(this.dispatcher).flatMap(function (d) { return d.changes().map(function (s) { return s.board; }); });
	        this.displayChannel.subscribe(function (ev) {
	            console.log("display channel ");
	            console.log(ev);
	            console.log("***************");
	        }, function (err) {
	            console.error(err);
	        });
	    }
	    AppImpl.dispatch = function (cmd) {
	        switch (cmd.type) {
	            case CommandID_1.default.startApplication: return Reducers.synchReducer(function (st) { return st; });
	            case CommandID_1.default.createPlayer:
	            case CommandID_1.default.playerLogon: return Reducers.playerLogon;
	            case CommandID_1.default.playCard: return Reducers.playCard;
	            case CommandID_1.default.acceptCompetition:
	            case CommandID_1.default.declineCompetition:
	            case CommandID_1.default.startCompetition: return Reducers.competitionCommands;
	            case CommandID_1.default.diplayPlayerDeck: return Reducers.synchReducer(Reducers.diplayPlayerDeck);
	            case CommandID_1.default.newDomainEvent: return Reducers.synchReducer(Reducers.newDomainEvent);
	            case CommandID_1.default.selectPlayerForCompetition: return Reducers.synchReducer(Reducers.selectPlayerForCompetition);
	            case CommandID_1.default.setCompetitionDeadline: return Reducers.synchReducer(Reducers.setCompetitionDeadline);
	            case CommandID_1.default.setCompetitionKind: return Reducers.synchReducer(Reducers.setCompetitionKind);
	            case CommandID_1.default.setCurrentGame: return Reducers.synchReducer(Reducers.setCurrentGame);
	            case CommandID_1.default.updateCompetitionState: return Reducers.synchReducer(Reducers.updateCompetionState);
	            case CommandID_1.default.updateGameState: return Reducers.synchReducer(Reducers.updateGameState);
	            case CommandID_1.default.updatePlayersState: return Reducers.synchReducer(Reducers.updatePlayersState);
	            default:
	                throw new Error("invalid command " + cmd);
	        }
	    };
	    AppImpl.prototype.exec = function (cmd) {
	        this.dispatcher.then(function (d) {
	            d.dispatch(cmd).catch(function (err) { return console.error(err); });
	        });
	    };
	    return AppImpl;
	}());


/***/ },
/* 3 */
/***/ function(module, exports) {

	"use strict";
	var CommandID;
	(function (CommandID) {
	    CommandID[CommandID["startApplication"] = 0] = "startApplication";
	    CommandID[CommandID["createPlayer"] = 1] = "createPlayer";
	    CommandID[CommandID["playerLogon"] = 2] = "playerLogon";
	    CommandID[CommandID["startCompetition"] = 3] = "startCompetition";
	    CommandID[CommandID["acceptCompetition"] = 4] = "acceptCompetition";
	    CommandID[CommandID["declineCompetition"] = 5] = "declineCompetition";
	    CommandID[CommandID["playCard"] = 6] = "playCard";
	    CommandID[CommandID["selectPlayerForCompetition"] = 7] = "selectPlayerForCompetition";
	    CommandID[CommandID["setCompetitionKind"] = 8] = "setCompetitionKind";
	    CommandID[CommandID["setCompetitionDeadline"] = 9] = "setCompetitionDeadline";
	    CommandID[CommandID["setCurrentGame"] = 10] = "setCurrentGame";
	    CommandID[CommandID["diplayPlayerDeck"] = 11] = "diplayPlayerDeck";
	    CommandID[CommandID["updateGameState"] = 12] = "updateGameState";
	    CommandID[CommandID["updatePlayersState"] = 13] = "updatePlayersState";
	    CommandID[CommandID["updateCompetitionState"] = 14] = "updateCompetitionState";
	    CommandID[CommandID["newDomainEvent"] = 15] = "newDomainEvent";
	})(CommandID || (CommandID = {}));
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = CommandID;


/***/ },
/* 4 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var Commands = __webpack_require__(12);
	var PlayersService_1 = __webpack_require__(13);
	var PlayerService_1 = __webpack_require__(42);
	var rest_fetch_1 = __webpack_require__(18);
	var ddd_briscola_model_1 = __webpack_require__(14);
	var Util_1 = __webpack_require__(41);
	function initialState(entryPoint) {
	    return rest_fetch_1.fetch(ddd_briscola_model_1.SiteMap).from(entryPoint).then(function (siteMap) {
	        return {
	            playersService: new PlayersService_1.PlayersService(siteMap),
	            playerService: flib_1.Option.none(),
	            board: ddd_briscola_model_1.Board.empty()
	        };
	    });
	}
	exports.initialState = initialState;
	function synchReducer(rt) {
	    return function (state, command, dispatch) { return Promise.resolve(rt(state, command, dispatch)); };
	}
	exports.synchReducer = synchReducer;
	exports.playerLogon = function (state, command, dispatch) {
	    if (command instanceof Commands.PlayerLogon || command instanceof Commands.CreatePlayer) {
	        var ps = state.playersService;
	        var createPlayer = (command instanceof Commands.PlayerLogon) ? ps.logon(command.playerName, command.password) : ps.createPlayer(command.playerName, command.password);
	        return createPlayer.then(function (player) {
	            var playerService = new PlayerService_1.PlayerService(player);
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
	            return {
	                playersService: state.playersService,
	                playerService: flib_1.Option.some(playerService),
	                board: board
	            };
	        });
	    }
	    else
	        return Promise.resolve(state);
	};
	exports.playCard = function (state, command) {
	    if (command instanceof Commands.PlayCard) {
	        return state.playerService.map(function (playerService) {
	            var game = state.board.currentGame.map(function (g) { return Promise.resolve(g.self); }).getOrElse(function () { return Promise.reject("no current game"); });
	            return game.then(function (game) {
	                playerService.playCard(game, command.card);
	                return {
	                    playersService: state.playersService,
	                    playerService: flib_1.Option.some(playerService),
	                    board: state.board
	                };
	            });
	        }).getOrElse(function () { return Promise.reject("player service not avaiable"); });
	    }
	    else {
	        return Promise.resolve(state);
	    }
	};
	function playerServiceEffect(effect) {
	    return function (state, command) {
	        return state.playerService.map(function (playerService) {
	            effect(playerService, state, command);
	            return Promise.resolve(state);
	        }).getOrElse(function () { return Promise.reject("player service not avaiable"); });
	    };
	}
	exports.competitionCommands = function (state, command, dispacth) {
	    var res;
	    if (command instanceof Commands.StartCompetition) {
	        res = playerServiceEffect(function (playerService) {
	            return playerService.createCompetition(Object.keys(state.board.competitionSelectedPlayers), state.board.competitionKind, state.board.competitionDeadlineKind);
	        });
	    }
	    else if (command instanceof Commands.AcceptCompetition) {
	        res = playerServiceEffect(function (playerService) { return playerService.acceptCompetition(command.competition); });
	    }
	    else if (command instanceof Commands.DeclineCompetition) {
	        res = playerServiceEffect(function (playerService) { return playerService.declineCompetition(command.competition); });
	    }
	    else {
	        res = function () { return Promise.reject("invalid command " + command.type + " "); };
	    }
	    return res(state, command, dispacth);
	};
	function boardReducer(br) {
	    return function (state, command) {
	        return {
	            playersService: state.playersService,
	            playerService: state.playerService,
	            board: br(state.board, command)
	        };
	    };
	}
	exports.selectPlayerForCompetition = boardReducer(function (board, command) {
	    if (command instanceof Commands.SelectPlayerForCompetition) {
	        if (command.selected) {
	            return Util_1.copy(board, {
	                competitionSelectedPlayers: (_a = {},
	                    _a[command.player] = true,
	                    _a
	                )
	            });
	        }
	        else {
	            var cpy = Util_1.copy(board, {});
	            delete cpy.competitionSelectedPlayers[command.player];
	            return cpy;
	        }
	    }
	    else {
	        return board;
	    }
	    var _a;
	});
	exports.setCompetitionKind = boardReducer(function (board, command) {
	    if (command instanceof Commands.SetCompetitionKind) {
	        return Util_1.copy(board, {
	            competitionKind: command.kind
	        });
	    }
	    else {
	        return board;
	    }
	});
	exports.setCompetitionDeadline = boardReducer(function (board, command) {
	    if (command instanceof Commands.SetCompetitionDeadline) {
	        return Util_1.copy(board, {
	            competitionDeadlineKind: command.deadlineKind
	        });
	    }
	    else {
	        return board;
	    }
	});
	exports.setCurrentGame = boardReducer(function (board, command) {
	    if (command instanceof Commands.SetCurrentGame) {
	        return Util_1.copy(board, {
	            currentGame: flib_1.Option.option(board.activeGames[command.game]).orElse(function () { return flib_1.Option.option(board.finishedGames[command.game]); })
	        });
	    }
	    else {
	        return board;
	    }
	});
	exports.diplayPlayerDeck = boardReducer(function (board, command) {
	    if (command instanceof Commands.DiplayPlayerDeck) {
	        return Util_1.copy(board, {
	            viewFlag: command.display === true ? ddd_briscola_model_1.ViewFlag.showPlayerCards : ddd_briscola_model_1.ViewFlag.normal
	        });
	    }
	    else {
	        return board;
	    }
	});
	exports.updatePlayersState = boardReducer(function (board, command) {
	    if (command instanceof Commands.UpdatePlayersState) {
	        return Util_1.copy(board, {
	            players: board.player.map(function (cp) { return command.players.filter(function (pl) { return pl.self !== cp.self; }); }).getOrElse(function () { return command.players; })
	        });
	    }
	    else {
	        return board;
	    }
	});
	exports.updateGameState = boardReducer(function (board, command) {
	    if (command instanceof Commands.UpdateGameState) {
	        var gm_1 = command.gameState;
	        var res = board;
	        var currentGame = board.currentGame.fold(function () { return flib_1.Option.some(gm_1); }, function (cgm) {
	            if (gm_1.self === cgm.self) {
	                return flib_1.Option.some(gm_1);
	            }
	            else {
	                return flib_1.Option.some(cgm);
	            }
	        });
	        if (gm_1 instanceof ddd_briscola_model_1.ActiveGameState) {
	            res = Util_1.copy(board, {
	                currentGame: currentGame,
	                activeGames: (_a = {},
	                    _a[gm_1.self] = gm_1,
	                    _a
	                )
	            });
	        }
	        else if (gm_1 instanceof ddd_briscola_model_1.FinalGameState) {
	            res = Util_1.copy(board, {
	                currentGame: currentGame,
	                finishedGames: (_b = {},
	                    _b[gm_1.self] = gm_1,
	                    _b
	                )
	            });
	            delete res.activeGames[gm_1.self];
	        }
	        return res;
	    }
	    else {
	        return board;
	    }
	    var _a, _b;
	});
	exports.updateCompetionState = boardReducer(function (board, command) {
	    if (command instanceof Commands.UpdateCompetitionState) {
	        return Util_1.copy(board, {
	            engagedCompetitions: (_a = {},
	                _a[command.competitionState.self] = command.competitionState,
	                _a
	            )
	        });
	    }
	    else {
	        return board;
	    }
	    var _a;
	});
	exports.newDomainEvent = boardReducer(function (board, command) {
	    if (command instanceof Commands.NewDomainEvent) {
	        return Util_1.copy(board, {
	            eventsLog: [command.event].concat(board.eventsLog)
	        });
	    }
	    else {
	        return board;
	    }
	});


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
	        return findIndex(arr, pred).map(function (idx) { return arr[idx]; });
	    }
	    Arrays.find = find;
	    function findIndex(arr, pred) {
	        for (var i = 0; i < arr.length; i++) {
	            var v = arr[i];
	            if (pred(v))
	                return Option_1.default.some(i);
	        }
	        return Option_1.default.none();
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
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var Option = (function () {
	    function Option() {
	    }
	    Option.some = function (v) {
	        return new SomeImpl(v);
	    };
	    Option.none = function () {
	        return None.instance;
	    };
	    Option.option = function (t) {
	        return (t !== null && t !== undefined) ? Option.some(t) : Option.none();
	    };
	    Option.prototype.fold = function (fnone, fsome) {
	        return fnone();
	    };
	    Option.prototype.map = function (f) {
	        return Option.none();
	    };
	    Option.prototype.forEach = function (f) {
	    };
	    Option.prototype.flatMap = function (f) {
	        return Option.none();
	    };
	    Option.prototype.getOrElse = function (f) {
	        return f();
	    };
	    Option.prototype.orElse = function (f) {
	        return f();
	    };
	    Option.prototype.isEmpty = function () {
	        return true;
	    };
	    Option.prototype.isDefined = function () {
	        return false;
	    };
	    Option.prototype.zip = function (b) {
	        return Option.none();
	    };
	    Option.prototype.toArray = function () {
	        return [];
	    };
	    return Option;
	}());
	exports.Option = Option;
	var None = (function (_super) {
	    __extends(None, _super);
	    function None() {
	        _super.apply(this, arguments);
	    }
	    None.instance = new None();
	    return None;
	}(Option));
	var SomeImpl = (function (_super) {
	    __extends(SomeImpl, _super);
	    function SomeImpl(value) {
	        _super.call(this);
	        this.value = value;
	    }
	    SomeImpl.prototype.isEmpty = function () { return false; };
	    SomeImpl.prototype.isDefined = function () { return true; };
	    SomeImpl.prototype.fold = function (fnone, fsome) {
	        return fsome(this.value);
	    };
	    SomeImpl.prototype.forEach = function (f) {
	        f(this.value);
	    };
	    SomeImpl.prototype.map = function (f) {
	        return Option.some(f(this.value));
	    };
	    SomeImpl.prototype.flatMap = function (f) {
	        return f(this.value);
	    };
	    SomeImpl.prototype.getOrElse = function (f) {
	        return this.value;
	    };
	    SomeImpl.prototype.orElse = function (f) {
	        return this;
	    };
	    SomeImpl.prototype.zip = function (b) {
	        var _this = this;
	        return b.map(function (t1) { return [_this.value, t1]; });
	    };
	    SomeImpl.prototype.toArray = function () {
	        return [this.value];
	    };
	    SomeImpl.prototype.toString = function () {
	        return "Some(" + this.value + ")";
	    };
	    return SomeImpl;
	}(Option));
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
	    var value = Option_1.default.none();
	    return function () {
	        return value.fold(function () {
	            var v = f();
	            value = Option_1.default.some(v);
	            return v;
	        }, function (v) { return v; });
	    };
	}
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
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var CommandID_1 = __webpack_require__(3);
	var Command = (function () {
	    function Command() {
	    }
	    return Command;
	}());
	exports.Command = Command;
	var StarApplication = (function (_super) {
	    __extends(StarApplication, _super);
	    function StarApplication() {
	        _super.call(this);
	        this.type = CommandID_1.default.startApplication;
	    }
	    return StarApplication;
	}(Command));
	exports.StarApplication = StarApplication;
	var CreatePlayer = (function (_super) {
	    __extends(CreatePlayer, _super);
	    function CreatePlayer(playerName, password) {
	        _super.call(this);
	        this.playerName = playerName;
	        this.password = password;
	        this.type = CommandID_1.default.createPlayer;
	    }
	    return CreatePlayer;
	}(Command));
	exports.CreatePlayer = CreatePlayer;
	var PlayerLogon = (function (_super) {
	    __extends(PlayerLogon, _super);
	    function PlayerLogon(playerName, password) {
	        _super.call(this);
	        this.playerName = playerName;
	        this.password = password;
	        this.type = CommandID_1.default.playerLogon;
	    }
	    return PlayerLogon;
	}(Command));
	exports.PlayerLogon = PlayerLogon;
	var StartCompetition = (function (_super) {
	    __extends(StartCompetition, _super);
	    function StartCompetition() {
	        _super.call(this);
	        this.type = CommandID_1.default.startCompetition;
	    }
	    return StartCompetition;
	}(Command));
	exports.StartCompetition = StartCompetition;
	var AcceptCompetition = (function (_super) {
	    __extends(AcceptCompetition, _super);
	    function AcceptCompetition(competition) {
	        _super.call(this);
	        this.competition = competition;
	        this.type = CommandID_1.default.acceptCompetition;
	    }
	    return AcceptCompetition;
	}(Command));
	exports.AcceptCompetition = AcceptCompetition;
	var DeclineCompetition = (function (_super) {
	    __extends(DeclineCompetition, _super);
	    function DeclineCompetition(competition) {
	        _super.call(this);
	        this.competition = competition;
	        this.type = CommandID_1.default.declineCompetition;
	    }
	    return DeclineCompetition;
	}(Command));
	exports.DeclineCompetition = DeclineCompetition;
	var PlayCard = (function (_super) {
	    __extends(PlayCard, _super);
	    function PlayCard(card) {
	        _super.call(this);
	        this.card = card;
	        this.type = CommandID_1.default.playCard;
	    }
	    return PlayCard;
	}(Command));
	exports.PlayCard = PlayCard;
	var SelectPlayerForCompetition = (function (_super) {
	    __extends(SelectPlayerForCompetition, _super);
	    function SelectPlayerForCompetition(player, selected) {
	        _super.call(this);
	        this.player = player;
	        this.selected = selected;
	        this.type = CommandID_1.default.selectPlayerForCompetition;
	    }
	    return SelectPlayerForCompetition;
	}(Command));
	exports.SelectPlayerForCompetition = SelectPlayerForCompetition;
	var SetCompetitionKind = (function (_super) {
	    __extends(SetCompetitionKind, _super);
	    function SetCompetitionKind(kind) {
	        _super.call(this);
	        this.kind = kind;
	        this.type = CommandID_1.default.setCompetitionKind;
	    }
	    return SetCompetitionKind;
	}(Command));
	exports.SetCompetitionKind = SetCompetitionKind;
	var SetCompetitionDeadline = (function (_super) {
	    __extends(SetCompetitionDeadline, _super);
	    function SetCompetitionDeadline(deadlineKind) {
	        _super.call(this);
	        this.deadlineKind = deadlineKind;
	        this.type = CommandID_1.default.setCompetitionDeadline;
	    }
	    return SetCompetitionDeadline;
	}(Command));
	exports.SetCompetitionDeadline = SetCompetitionDeadline;
	var SetCurrentGame = (function (_super) {
	    __extends(SetCurrentGame, _super);
	    function SetCurrentGame(game) {
	        _super.call(this);
	        this.game = game;
	        this.type = CommandID_1.default.setCurrentGame;
	    }
	    return SetCurrentGame;
	}(Command));
	exports.SetCurrentGame = SetCurrentGame;
	var DiplayPlayerDeck = (function (_super) {
	    __extends(DiplayPlayerDeck, _super);
	    function DiplayPlayerDeck(game, display) {
	        _super.call(this);
	        this.game = game;
	        this.display = display;
	        this.type = CommandID_1.default.diplayPlayerDeck;
	    }
	    return DiplayPlayerDeck;
	}(Command));
	exports.DiplayPlayerDeck = DiplayPlayerDeck;
	var UpdateGameState = (function (_super) {
	    __extends(UpdateGameState, _super);
	    function UpdateGameState(gameState) {
	        _super.call(this);
	        this.gameState = gameState;
	        this.type = CommandID_1.default.updateGameState;
	    }
	    return UpdateGameState;
	}(Command));
	exports.UpdateGameState = UpdateGameState;
	var UpdatePlayersState = (function (_super) {
	    __extends(UpdatePlayersState, _super);
	    function UpdatePlayersState(players) {
	        _super.call(this);
	        this.players = players;
	        this.type = CommandID_1.default.updatePlayersState;
	    }
	    return UpdatePlayersState;
	}(Command));
	exports.UpdatePlayersState = UpdatePlayersState;
	var UpdateCompetitionState = (function (_super) {
	    __extends(UpdateCompetitionState, _super);
	    function UpdateCompetitionState(competitionState) {
	        _super.call(this);
	        this.competitionState = competitionState;
	        this.type = CommandID_1.default.updateCompetitionState;
	    }
	    return UpdateCompetitionState;
	}(Command));
	exports.UpdateCompetitionState = UpdateCompetitionState;
	var NewDomainEvent = (function (_super) {
	    __extends(NewDomainEvent, _super);
	    function NewDomainEvent(event) {
	        _super.call(this);
	        this.event = event;
	        this.type = CommandID_1.default.newDomainEvent;
	    }
	    return NewDomainEvent;
	}(Command));
	exports.NewDomainEvent = NewDomainEvent;


/***/ },
/* 13 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var ddd_briscola_model_1 = __webpack_require__(14);
	var rest_fetch_1 = __webpack_require__(18);
	var Util_1 = __webpack_require__(41);
	var PlayersService = (function () {
	    function PlayersService(siteMap) {
	        this.siteMap = siteMap;
	    }
	    PlayersService.prototype.allPlayers = function () {
	        return rest_fetch_1.fetch(ddd_briscola_model_1.PlayersCollection).from(this.siteMap.players).then(function (c) { return c.members; });
	    };
	    PlayersService.prototype.player = function (playerSelf) {
	        return rest_fetch_1.fetch(ddd_briscola_model_1.Player).from(playerSelf);
	    };
	    PlayersService.prototype.createPlayer = function (name, password) {
	        return Util_1.Http.POST(this.siteMap.players, {
	            name: name,
	            password: password
	        }).then(function (resp) {
	            return resp.json().then(function (pl) { return rest_fetch_1.fetch(ddd_briscola_model_1.CurrentPlayer).fromObject(pl); });
	        });
	    };
	    PlayersService.prototype.logon = function (name, password) {
	        return Util_1.Http.POST(this.siteMap.playerLogin, {
	            name: name,
	            password: password
	        }).then(function (resp) { return resp.json().then(function (pl) { return rest_fetch_1.fetch(ddd_briscola_model_1.CurrentPlayer).fromObject(pl); }); });
	    };
	    return PlayersService;
	}());
	exports.PlayersService = PlayersService;


/***/ },
/* 14 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	function __export(m) {
	    for (var p in m) if (!exports.hasOwnProperty(p)) exports[p] = m[p];
	}
	__export(__webpack_require__(15));
	__export(__webpack_require__(16));
	__export(__webpack_require__(35));
	__export(__webpack_require__(36));
	__export(__webpack_require__(37));
	__export(__webpack_require__(17));
	__export(__webpack_require__(34));
	__export(__webpack_require__(38));
	__export(__webpack_require__(39));
	var Input = __webpack_require__(40);
	exports.Input = Input;


/***/ },
/* 15 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var Board;
	(function (Board) {
	    function empty() {
	        return {
	            player: flib_1.Option.none(),
	            players: [],
	            currentGame: flib_1.Option.none(),
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
/* 16 */
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
	var model_1 = __webpack_require__(17);
	var player_1 = __webpack_require__(34);
	var rest_fetch_1 = __webpack_require__(18);
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
	exports.toCompetitionStartDeadline = rest_fetch_1.Selector.create(function (wso) {
	    switch (wso.kind) {
	        case CompetitionStartDeadlineKind[CompetitionStartDeadlineKind.allPlayers]: return flib_1.Option.some(AllPlayers);
	        case CompetitionStartDeadlineKind[CompetitionStartDeadlineKind.onPlayerCount]: return flib_1.Option.some(OnPlayerCount);
	        default: throw new Error("invalid CompetitionStartDeadline : " + JSON.stringify(wso));
	    }
	});
	var stringToCompetitionStartDeadlineKind = rest_fetch_1.SimpleConverter.fromString.andThen(rest_fetch_1.Converter.toEnum(CompetitionStartDeadlineKind, "CompetitionStartDeadlineKind"));
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
	exports.toMatchKind = rest_fetch_1.Selector.create(function (wso) {
	    switch (wso.kind) {
	        case MatchKindKind[MatchKindKind.singleMatch]: return flib_1.Option.some(SingleMatch);
	        case MatchKindKind[MatchKindKind.numberOfGamesMatchKind]: return flib_1.Option.some(NumberOfGamesMatchKind);
	        case MatchKindKind[MatchKindKind.targetPointsMatchKind]: return flib_1.Option.some(TargetPointsMatchKind);
	        default: return flib_1.Option.none();
	    }
	}, "match kind");
	var stringToMatchKindKind = rest_fetch_1.SimpleConverter.fromString.andThen(rest_fetch_1.Converter.toEnum(MatchKindKind, "MatchKindKind"));
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
	        rest_fetch_1.link({ arrayOf: player_1.Player }), 
	        __metadata('design:type', Array)
	    ], Competition.prototype, "players", void 0);
	    __decorate([
	        rest_fetch_1.convert(exports.toMatchKind), 
	        __metadata('design:type', Object)
	    ], Competition.prototype, "kind", void 0);
	    __decorate([
	        rest_fetch_1.convert(exports.toCompetitionStartDeadline), 
	        __metadata('design:type', Object)
	    ], Competition.prototype, "deadline", void 0);
	    return Competition;
	}());
	exports.Competition = Competition;
	var stringToCompetitionStateKind = rest_fetch_1.SimpleConverter.fromString.andThen(rest_fetch_1.Converter.toEnum(CompetitionStateKind, "CompetitionStateKind"));
	var CompetitionState = (function () {
	    function CompetitionState() {
	    }
	    __decorate([
	        rest_fetch_1.convert(stringToCompetitionStateKind), 
	        __metadata('design:type', Number)
	    ], CompetitionState.prototype, "kind", void 0);
	    __decorate([
	        rest_fetch_1.convert(), 
	        __metadata('design:type', Competition)
	    ], CompetitionState.prototype, "competition", void 0);
	    __decorate([
	        rest_fetch_1.link({ arrayOf: player_1.Player }), 
	        __metadata('design:type', Array)
	    ], CompetitionState.prototype, "acceptingPlayers", void 0);
	    __decorate([
	        rest_fetch_1.link({ arrayOf: player_1.Player }), 
	        __metadata('design:type', Array)
	    ], CompetitionState.prototype, "decliningPlayers", void 0);
	    __decorate([
	        rest_fetch_1.convert(rest_fetch_1.SimpleConverter.optional(rest_fetch_1.SimpleConverter.fromString)), 
	        __metadata('design:type', flib_1.Option)
	    ], CompetitionState.prototype, "accept", void 0);
	    __decorate([
	        rest_fetch_1.convert(rest_fetch_1.SimpleConverter.optional(rest_fetch_1.SimpleConverter.fromString)), 
	        __metadata('design:type', flib_1.Option)
	    ], CompetitionState.prototype, "decline", void 0);
	    return CompetitionState;
	}());
	exports.CompetitionState = CompetitionState;
	exports.competitionStateChoice = model_1.byKindChoice(function () { return [{
	        key: CompetitionStateKind[CompetitionStateKind.dropped],
	        value: CompetitionState
	    }, {
	        key: CompetitionStateKind[CompetitionStateKind.fullfilled],
	        value: CompetitionState
	    }, {
	        key: CompetitionStateKind[CompetitionStateKind.open],
	        value: CompetitionState
	    }]; }, "competion state");


/***/ },
/* 17 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var rest_fetch_1 = __webpack_require__(18);
	function byKindChoice(bks, desc) {
	    var mp = flib_1.JsMap.create(bks());
	    return rest_fetch_1.ByPropertySelector.fromEntries(function (wso) { return wso.kind; }, bks, desc);
	}
	exports.byKindChoice = byKindChoice;


/***/ },
/* 18 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var Link_1 = __webpack_require__(19);
	exports.link = Link_1.link;
	var Fetch_1 = __webpack_require__(26);
	exports.fetch = Fetch_1.fetch;
	exports.fetchChoose = Fetch_1.fetchChoose;
	var Converter = __webpack_require__(30);
	exports.Converter = Converter;
	var Converter_1 = __webpack_require__(30);
	exports.convert = Converter_1.convert;
	var cache_1 = __webpack_require__(29);
	exports.ByUrlCache = cache_1.ByUrlCache;
	var SimpleConverter_1 = __webpack_require__(21);
	exports.SimpleConverter = SimpleConverter_1.SimpleConverter;
	var Selector_1 = __webpack_require__(22);
	exports.Selector = Selector_1.Selector;
	exports.ByPropertySelector = Selector_1.ByPropertySelector;


/***/ },
/* 19 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var types_1 = __webpack_require__(20);
	var TypeExpr_1 = __webpack_require__(23);
	var Meta_1 = __webpack_require__(24);
	var flib_1 = __webpack_require__(5);
	var Link = (function () {
	    function Link(resultType, targetProperty) {
	        this.resultType = resultType;
	        this.targetProperty = targetProperty;
	        if (flib_1.isNull(resultType)) {
	            throw new Error("undefined resultType");
	        }
	        if (flib_1.isNull(targetProperty)) {
	            throw new Error("undefined targetProperty");
	        }
	    }
	    return Link;
	}());
	exports.Link = Link;
	function getMappingType(typ, propType, errorPrefix) {
	    var typIsNull = flib_1.isNull(typ);
	    var propTypeIsArray = types_1.isArrayType(propType);
	    var typIsArrayMappingType = types_1.MappingType.isArrayMappingType(typ);
	    if (propTypeIsArray && !typIsArrayMappingType)
	        throw new Error(errorPrefix() + ": Array property with undefined 'arrayOf' property");
	    else if (!propTypeIsArray && typIsArrayMappingType)
	        throw new Error(errorPrefix() + ": with link of array type a property of type 'Array' is required");
	    var propTypeIsOption = types_1.isOptionType(propType);
	    var typIsOptionMappingType = types_1.MappingType.isOptionMappingType(typ);
	    if (propTypeIsOption && !typIsOptionMappingType)
	        throw new Error(errorPrefix() + ": Option property with undefined 'optionOf' property");
	    else if (!propTypeIsOption && typIsOptionMappingType)
	        throw new Error(errorPrefix() + ": with link of option type a property of type 'Option' is required");
	    var typIsMappingType = types_1.MappingType.isMappingType(typ);
	    var linkType = typIsMappingType ? typ : propType;
	    if (flib_1.isNull(linkType)) {
	        throw new Error(errorPrefix() + " : could not infer type");
	    }
	    var linkJsTyp;
	    try {
	        linkJsTyp = TypeExpr_1.TypeExpr.fromMappingType(linkType);
	    }
	    catch (e) {
	        throw new Error(errorPrefix() + " : " + e.message);
	    }
	    return linkJsTyp;
	}
	function link(typ, opts1) {
	    return function (target, key) {
	        var propType = Reflect.getMetadata("design:type", target, key);
	        var errorPrefix = flib_1.lazy(function () { return ("Error on property '" + key + "' in class '" + target.constructor.name + "'"); });
	        var linkJsTyp = getMappingType(typ, propType, errorPrefix);
	        var objLinks = Meta_1.getOrCreateLinksMeta(target.constructor);
	        var targetProperty = (opts1 && opts1.property) || (typ && typ["property"]) || key;
	        Meta_1.addObjectMapping(objLinks, targetProperty, new Link(linkJsTyp, key));
	    };
	}
	exports.link = link;


/***/ },
/* 20 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var SimpleConverter_1 = __webpack_require__(21);
	var Selector_1 = __webpack_require__(22);
	var flib_1 = __webpack_require__(5);
	var GetPropertyUrl = (function () {
	    function GetPropertyUrl(convert) {
	        this.convert = convert;
	    }
	    GetPropertyUrl.prototype.andThen = function (f) {
	        var _this = this;
	        return new GetPropertyUrl(function (a) { return f(_this.convert(a)); });
	    };
	    return GetPropertyUrl;
	}());
	exports.GetPropertyUrl = GetPropertyUrl;
	var MappingType;
	(function (MappingType) {
	    function isMappingType(t) {
	        var notIsPrim = !isPrimitiveType(t);
	        return !flib_1.isNull(t) && notIsPrim && ((typeof t === "function") ||
	            (t instanceof SimpleConverter_1.SimpleConverter) || (t instanceof Selector_1.Selector) || (t instanceof GetPropertyUrl) ||
	            isArrayMappingType(t) || isOptionMappingType(t));
	    }
	    MappingType.isMappingType = isMappingType;
	    function trapBoolean(f) {
	        try {
	            return f();
	        }
	        catch (e) {
	            return false;
	        }
	    }
	    function hasMappingTypeProperty(a, property, desc) {
	        if (!flib_1.isNull(a) && a.hasOwnProperty(property)) {
	            var itmType = a[property];
	            var itmIsJsTyp = isMappingType(itmType);
	            //if (!itmIsJsTyp) throw new Error(`${desc} is invalid :${itmType}`)
	            return itmIsJsTyp;
	        }
	        else
	            return false;
	    }
	    function isArrayMappingType(a) {
	        return hasMappingTypeProperty(a, "arrayOf", "arrayOf parameter");
	    }
	    MappingType.isArrayMappingType = isArrayMappingType;
	    function isOptionMappingType(a) {
	        return hasMappingTypeProperty(a, "optionOf", "optionOf parameter");
	    }
	    MappingType.isOptionMappingType = isOptionMappingType;
	    function fold(typ, simple, array, option, choose, propertyUrl) {
	        return function (t) {
	            if (flib_1.isNull(t))
	                throw new Error("undefined MappingType ");
	            if (t instanceof SimpleConverter_1.SimpleConverter)
	                return simple(t);
	            else if (t instanceof Selector_1.Selector)
	                return choose(t);
	            else if (t instanceof GetPropertyUrl)
	                return propertyUrl(t);
	            else if (typeof t === "function") {
	                if (!isPrimitiveType(t))
	                    return typ(t);
	                else
	                    throw new Error("invalid constructor type " + t);
	            }
	            else if (isArrayMappingType(t))
	                return array(t);
	            else if (isOptionMappingType(t))
	                return option(t);
	            else
	                throw new Error("unrecognized MappingType " + JSON.stringify(t));
	        };
	    }
	    MappingType.fold = fold;
	    MappingType.description = fold(function (ct) { return ("object " + ct.name); }, function (simpleCnv) { return ("simple converter " + simpleCnv.description); }, function (arryCnv) { return ("array of " + MappingType.description(arryCnv.arrayOf)); }, function (optionCnv) { return ("option of " + MappingType.description(optionCnv.optionOf)); }, function (choose) { return ("choice " + choose.description); }, function (propUrl) { return "Property Url"; });
	})(MappingType = exports.MappingType || (exports.MappingType = {}));
	function isPrimitiveType(a) {
	    return a === String || a === Number || a === Boolean;
	}
	exports.isPrimitiveType = isPrimitiveType;
	function eq(a, b) {
	    return a === b;
	}
	function isOptionType(a) {
	    return eq(a, flib_1.Option);
	}
	exports.isOptionType = isOptionType;
	function isArrayType(a) {
	    return eq(a, Array);
	}
	exports.isArrayType = isArrayType;


/***/ },
/* 21 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var flib_1 = __webpack_require__(5);
	var SimpleConverter = (function () {
	    function SimpleConverter(convert, description) {
	        this.convert = convert;
	        this.description = description;
	    }
	    SimpleConverter.prototype.andThen = function (f) {
	        var _this = this;
	        return new SimpleConverter(function (a) { return f(_this.convert(a)); });
	    };
	    SimpleConverter.prototype.compose = function (f) {
	        var _this = this;
	        return new SimpleConverter(function (a) { return _this.convert(f(a)); });
	    };
	    return SimpleConverter;
	}());
	exports.SimpleConverter = SimpleConverter;
	var FromTypeConverter = (function (_super) {
	    __extends(FromTypeConverter, _super);
	    function FromTypeConverter(pred, description) {
	        _super.call(this, function (a) {
	            if (pred(a))
	                return a;
	            else
	                throw new Error("error, " + a + " is not a " + description);
	        });
	        this.description = description;
	    }
	    return FromTypeConverter;
	}(SimpleConverter));
	var SimpleConverter;
	(function (SimpleConverter) {
	    SimpleConverter.identity = new SimpleConverter(function (id) { return id; }, "identity");
	    function fromTypeOf(typeString) {
	        return new FromTypeConverter(function (a) { return typeof a === typeString; }, typeString);
	    }
	    SimpleConverter.fromTypeOf = fromTypeOf;
	    SimpleConverter.fromString = fromTypeOf("string");
	    SimpleConverter.fromNumber = fromTypeOf("number");
	    SimpleConverter.fromBoolean = fromTypeOf("boolean");
	    SimpleConverter.fromUndefined = fromTypeOf("undefined");
	    function optional(cnv) {
	        return new SimpleConverter(function (a) {
	            if (flib_1.isNull(a))
	                return flib_1.Option.none();
	            else
	                return flib_1.Option.option(cnv.convert(a));
	        });
	    }
	    SimpleConverter.optional = optional;
	    function fromArray(c) {
	        var desc = flib_1.isNull(c.description) ? "array" : "array of " + c.description;
	        return new SimpleConverter(function (a) {
	            if (Array.isArray(a))
	                return a.map(function (i) { return c.convert(i); });
	            else
	                throw new Error("error, " + a + " is not a " + desc);
	        }, desc);
	    }
	    SimpleConverter.fromArray = fromArray;
	})(SimpleConverter = exports.SimpleConverter || (exports.SimpleConverter = {}));


/***/ },
/* 22 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var flib_1 = __webpack_require__(5);
	var Selector = (function () {
	    function Selector(convert, description) {
	        this.convert = convert;
	        this.description = description;
	    }
	    return Selector;
	}());
	exports.Selector = Selector;
	var Selector;
	(function (Selector) {
	    function create(f, description) {
	        return new Selector(f, description);
	    }
	    Selector.create = create;
	    function value(ct) {
	        return create(function (a) { return flib_1.Option.some(ct); });
	    }
	    Selector.value = value;
	    function byPropertyExists(entries, description) {
	        return new Selector(function (ws) { return flib_1.Arrays.find(entries, function (e) { return !flib_1.isNull(ws[e.key]); }).map(function (e) { return e.value; }); }, description);
	    }
	    Selector.byPropertyExists = byPropertyExists;
	})(Selector = exports.Selector || (exports.Selector = {}));
	var ByPropertySelector = (function (_super) {
	    __extends(ByPropertySelector, _super);
	    function ByPropertySelector(prop, jsMap, desc) {
	        _super.call(this, function (a) {
	            var pv = prop(a);
	            return flib_1.Option.option(jsMap()[pv]);
	        }, desc);
	        this.jsMap = jsMap;
	    }
	    ByPropertySelector.prototype.contains = function (nm) {
	        return !flib_1.isNull(this.jsMap()[nm]);
	    };
	    return ByPropertySelector;
	}(Selector));
	exports.ByPropertySelector = ByPropertySelector;
	var ByPropertySelector;
	(function (ByPropertySelector) {
	    function fromEntries(prop, bks, desc) {
	        var mp = flib_1.lazy(function () { return flib_1.JsMap.create(bks()); });
	        return new ByPropertySelector(prop, mp, desc);
	    }
	    ByPropertySelector.fromEntries = fromEntries;
	})(ByPropertySelector = exports.ByPropertySelector || (exports.ByPropertySelector = {}));


/***/ },
/* 23 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var types_1 = __webpack_require__(20);
	var SimpleConverter_1 = __webpack_require__(21);
	var Selector_1 = __webpack_require__(22);
	(function (TypeExprKind) {
	    TypeExprKind[TypeExprKind["constructorFunction"] = 0] = "constructorFunction";
	    TypeExprKind[TypeExprKind["simpleConverter"] = 1] = "simpleConverter";
	    TypeExprKind[TypeExprKind["array"] = 2] = "array";
	    TypeExprKind[TypeExprKind["option"] = 3] = "option";
	    TypeExprKind[TypeExprKind["choice"] = 4] = "choice";
	    TypeExprKind[TypeExprKind["getPropertyUrl"] = 5] = "getPropertyUrl";
	})(exports.TypeExprKind || (exports.TypeExprKind = {}));
	var TypeExprKind = exports.TypeExprKind;
	var TypeExpr = (function () {
	    function TypeExpr(kind, folding, leaf) {
	        this.kind = kind;
	        this.folding = folding;
	        this.leaf = leaf;
	    }
	    TypeExpr.prototype.equalTo = function (b) {
	        return this.kind === b.kind && this.folding === b.folding && this.leaf === b.leaf;
	    };
	    Object.defineProperty(TypeExpr.prototype, "value", {
	        get: function () {
	            if (this.folding.length === 0)
	                return this.leaf;
	            else {
	                var rest = this.folding.substr(1);
	                if (rest.length > 0) {
	                    var hd = rest.charAt(0);
	                    switch (hd) {
	                        case "a": return new TypeExpr(TypeExprKind.array, rest, this.leaf);
	                        case "o": return new TypeExpr(TypeExprKind.option, rest, this.leaf);
	                        default: throw new Error("invalid folding " + hd);
	                    }
	                }
	                else {
	                    return TypeExpr.fromMappingType(this.leaf);
	                }
	            }
	        },
	        enumerable: true,
	        configurable: true
	    });
	    Object.defineProperty(TypeExpr.prototype, "description", {
	        get: function () {
	            var _this = this;
	            return TypeExpr.fold(function (ct) { return LeafTypeExpr.description(ct); }, function (cnv) { return LeafTypeExpr.description(cnv); }, function () {
	                var v = _this.value;
	                if (v instanceof TypeExpr)
	                    return "Array(" + v.description + ")";
	                else
	                    return LeafTypeExpr.description(v);
	            }, function () {
	                var v = _this.value;
	                if (v instanceof TypeExpr)
	                    return "Option(" + v.description + ")";
	                else
	                    return LeafTypeExpr.description(v);
	            }, function (chs) { return "Selector"; }, function (pu) { return LeafTypeExpr.description(pu); })(this);
	        },
	        enumerable: true,
	        configurable: true
	    });
	    return TypeExpr;
	}());
	exports.TypeExpr = TypeExpr;
	var LeafTypeExpr;
	(function (LeafTypeExpr) {
	    function fold(typ, simple, choose, propertyUrl) {
	        return function (t) {
	            if (t instanceof SimpleConverter_1.SimpleConverter)
	                return simple(t);
	            else if (t instanceof Selector_1.Selector) {
	                return choose(t);
	            }
	            else if (t instanceof types_1.GetPropertyUrl)
	                return propertyUrl(t);
	            else if (typeof t === "function")
	                return typ(t);
	            else
	                throw new Error("Invalid Leaf Type Expr " + t);
	        };
	    }
	    LeafTypeExpr.fold = fold;
	    function description(l) {
	        return fold(function (ct) { return ct.name; }, function (cnv) { return cnv.description || "<SimpleConverter>"; }, function (chs) { return chs.description || "<Selector>"; }, function () { return "PropertyUrl"; })(l);
	    }
	    LeafTypeExpr.description = description;
	})(LeafTypeExpr = exports.LeafTypeExpr || (exports.LeafTypeExpr = {}));
	var TypeExpr;
	(function (TypeExpr) {
	    function create(ct) {
	        return new TypeExpr(TypeExprKind.constructorFunction, "", ct);
	    }
	    TypeExpr.create = create;
	    TypeExpr.fromMappingType = types_1.MappingType.fold(function (ct) { return new TypeExpr(TypeExprKind.constructorFunction, "", ct); }, function (cnv) { return new TypeExpr(TypeExprKind.simpleConverter, "", cnv); }, function (arr) {
	        var itmTypeExpr = TypeExpr.fromMappingType(arr.arrayOf);
	        return new TypeExpr(TypeExprKind.array, "a" + itmTypeExpr.folding, itmTypeExpr.leaf);
	    }, function (opt) {
	        var itmTypeExpr = TypeExpr.fromMappingType(opt.optionOf);
	        return new TypeExpr(TypeExprKind.option, "o" + itmTypeExpr.folding, itmTypeExpr.leaf);
	    }, function (chs) { return new TypeExpr(TypeExprKind.choice, "", chs); }, function (gpu) { return new TypeExpr(TypeExprKind.getPropertyUrl, "", gpu); });
	    function fold(typ, simple, array, option, choose, propertyUrl) {
	        return function (t) {
	            switch (t.kind) {
	                case TypeExprKind.constructorFunction: return typ(t.value);
	                case TypeExprKind.simpleConverter: return simple(t.value);
	                case TypeExprKind.array: return array(t);
	                case TypeExprKind.option: return option(t);
	                case TypeExprKind.choice:
	                    var chCnv_1 = t.value;
	                    var f = function (ws) {
	                        return chCnv_1.convert(ws).map(function (t) { return TypeExpr.fromMappingType(t); });
	                    };
	                    return choose(f);
	                case TypeExprKind.getPropertyUrl: return propertyUrl(t.value);
	            }
	        };
	    }
	    TypeExpr.fold = fold;
	    function foldExt(typ, simple, array, option, choose, propertyUrl) {
	        return function (t) {
	            if (t instanceof TypeExpr)
	                return fold(typ, simple, array, option, choose, propertyUrl)(t);
	            else
	                LeafTypeExpr.fold(function (ct) { return typ(ct); }, function (cnv) { return simple(cnv); }, function (chs) {
	                    var chCnv = t;
	                    var f = function (ws) { return chs.convert(ws).map(function (t) { return TypeExpr.fromMappingType(t); }); };
	                    return choose(f);
	                }, function (pu) { return propertyUrl(pu); });
	        };
	    }
	    TypeExpr.foldExt = foldExt;
	})(TypeExpr = exports.TypeExpr || (exports.TypeExpr = {}));


/***/ },
/* 24 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var EntriesMap_1 = __webpack_require__(25);
	var MetaLinksMap = (function () {
	    function MetaLinksMap() {
	        this.linksMeta = new EntriesMap_1.EntriesMap(function (a, b) { return a === b; });
	    }
	    MetaLinksMap.prototype.get = function (ct) {
	        return this.linksMeta.get(ct.name, ct);
	    };
	    MetaLinksMap.prototype.store = function (ct, v) {
	        this.linksMeta.store(ct.name, ct, v);
	    };
	    return MetaLinksMap;
	}());
	exports.MetaLinksMap = MetaLinksMap;
	var ExternalStrategy;
	(function (ExternalStrategy) {
	    /**
	    * FIXME
	    * refactor folling methods
	    */
	    var linksMeta = new MetaLinksMap();
	    function copyObjectMapping(o) {
	        var res = {};
	        Object.keys(o).forEach(function (k) { return res[k] = o[k].map(function (c) { return c; }); });
	        return res;
	    }
	    function superClassConstructor(c) {
	        return c.prototype.__proto__.constructor;
	    }
	    function getLinksMeta(c) {
	        return linksMeta.get(c);
	    }
	    ExternalStrategy.getLinksMeta = getLinksMeta;
	    function getOrCreateLinksMeta(c) {
	        return linksMeta.get(c).fold(function () {
	            var sup = getLinksMeta(superClassConstructor(c));
	            var lnks = sup.map(function (sup) { return copyObjectMapping(sup); }).getOrElse(function () { return {}; });
	            linksMeta.store(c, lnks);
	            return lnks;
	        }, function (v) { return v; });
	    }
	    ExternalStrategy.getOrCreateLinksMeta = getOrCreateLinksMeta;
	})(ExternalStrategy = exports.ExternalStrategy || (exports.ExternalStrategy = {}));
	function addObjectMapping(mp, k, v) {
	    flib_1.Option.option(mp[k]).fold(function () { return mp[k] = [v]; }, function (entries) { return entries.push(v); });
	}
	exports.addObjectMapping = addObjectMapping;
	exports.getLinksMeta = ExternalStrategy.getLinksMeta;
	exports.getOrCreateLinksMeta = ExternalStrategy.getOrCreateLinksMeta;


/***/ },
/* 25 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var EntriesMap = (function () {
	    function EntriesMap(keyPredicate) {
	        this.keyPredicate = keyPredicate;
	        this.cache = {};
	    }
	    EntriesMap.prototype.get = function (ks, k) {
	        var _this = this;
	        return flib_1.Option.option(this.cache[ks]).flatMap(function (entries) {
	            return flib_1.Arrays.find(entries, function (e) { return _this.keyPredicate(e.key, k); });
	        }).map(function (e) { return e.value; });
	    };
	    EntriesMap.prototype.store = function (ks, k, v) {
	        var _this = this;
	        flib_1.Option.option(this.cache[ks]).fold(function () { _this.cache[ks] = [{ key: k, value: v }]; }, function (entries) {
	            flib_1.Arrays.find(entries, function (e) { return _this.keyPredicate(e.key, k); }).fold(function () { return entries.push({ key: k, value: v }); }, function (oldValue) {
	                throw new Error("replacing previous entry " + ks + " key " + k);
	            });
	        });
	    };
	    return EntriesMap;
	}());
	exports.EntriesMap = EntriesMap;


/***/ },
/* 26 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var ResponseReader_1 = __webpack_require__(27);
	var RequestFactory_1 = __webpack_require__(28);
	var cache_1 = __webpack_require__(29);
	var Meta_1 = __webpack_require__(24);
	var Link_1 = __webpack_require__(19);
	var Converter_1 = __webpack_require__(30);
	var types_1 = __webpack_require__(20);
	var Util_1 = __webpack_require__(32);
	var InternalConversion_1 = __webpack_require__(31);
	var ObjectsCache_1 = __webpack_require__(33);
	var TypeExpr_1 = __webpack_require__(23);
	function createResourceRetriever(typ, opts) {
	    return new ResourceRetrieverImpl(typ, createContext(opts));
	}
	function fetch(typ, opts) {
	    return createResourceRetriever(typ, opts);
	}
	exports.fetch = fetch;
	function fetchArray(typ, opts) {
	    return createResourceRetriever({ arrayOf: typ }, opts);
	}
	exports.fetchArray = fetchArray;
	function fetchChoose(ch, opts) {
	    return createResourceRetriever(ch, opts);
	}
	exports.fetchChoose = fetchChoose;
	function createContext(opts) {
	    var requestFactory = (opts && opts.requestFactory) || RequestFactory_1.createRequestFactory({ method: "GET" });
	    var httpCache = (opts && opts.httpCache && opts.httpCache()) || new cache_1.ByUrlCache();
	    var cache = new ObjectsCache_1.ObjectsCache();
	    var responseReader = (opts && opts.responseReader) || ResponseReader_1.jsonResponseReader;
	    var context = { cache: cache, httpCache: httpCache, requestFactory: requestFactory, responseReader: responseReader, promisesToWait: [] };
	    return context;
	}
	function objectFetcherImpl(context) {
	    return function (typ, wso) {
	        return fetchProperties(context, typ, wso);
	    };
	}
	var ResourceRetrieverImpl = (function () {
	    function ResourceRetrieverImpl(typ, context) {
	        var _this = this;
	        this.typ = typ;
	        this.context = context;
	        this.objectFetcher = flib_1.lazy(function () { return objectFetcherImpl(_this.context); });
	        this.typeExpr = flib_1.lazy(function () { return TypeExpr_1.TypeExpr.fromMappingType(_this.typ); });
	        this.cnv = flib_1.lazy(function () { return InternalConversion_1.jsTypeToInternalConversion(_this.typ); });
	        this.isArrayMappingType = flib_1.lazy(function () { return _this.typeExpr().kind === TypeExpr_1.TypeExprKind.array; });
	    }
	    ResourceRetrieverImpl.prototype.waitPendingPromisesAndReturn = function (v) {
	        return Promise.all(this.context.promisesToWait).then(function (u) { return Promise.resolve(v); });
	    };
	    ResourceRetrieverImpl.prototype.fromObject = function (wso) {
	        var _this = this;
	        return this.cnv()(wso, this.objectFetcher(), undefined).then(function (v) { return _this.waitPendingPromisesAndReturn(v); });
	    };
	    ResourceRetrieverImpl.prototype.fromArray = function (wsos) {
	        var _this = this;
	        if (this.isArrayMappingType()) {
	            return this.cnv()(wsos, this.objectFetcher(), undefined).then(function (v) { return _this.waitPendingPromisesAndReturn(v); });
	        }
	        else {
	            return new ResourceRetrieverImpl({ arrayOf: this.typ }, this.context).fromArray(wsos);
	        }
	    };
	    ResourceRetrieverImpl.prototype.from = function (url, req) {
	        var _this = this;
	        if (this.isArrayMappingType()) {
	            return Promise.reject(new Error("trying to get an ArrayMappingType from " + url));
	        }
	        else {
	            var rq = req !== undefined ? new Request(url, req) : this.context.requestFactory(url);
	            var res_1 = undefined;
	            return fetchLink(url, this.context, this.typeExpr(), function (v) { return res_1 = v; }, undefined).then(function (v) { return _this.waitPendingPromisesAndReturn(res_1); });
	        }
	    };
	    ResourceRetrieverImpl.prototype.fromUrls = function (urls) {
	        var _this = this;
	        if (this.isArrayMappingType()) {
	            var res_2 = undefined;
	            return fetchLink(urls, this.context, this.typeExpr(), function (v) { return res_2 = v; }, undefined).then(function (v) { return _this.waitPendingPromisesAndReturn(res_2); });
	        }
	        else {
	            return new ResourceRetrieverImpl({ arrayOf: this.typ }, this.context).fromUrls(urls);
	        }
	    };
	    return ResourceRetrieverImpl;
	}());
	function httpFetch(req, httpCache, responseReader) {
	    return httpCache.get(req).fold(function () {
	        var r = window.fetch(req.url, req).then(function (resp) { return responseReader(resp, req); });
	        httpCache.store(req, r);
	        return r;
	    }, function (v) { return v; });
	}
	function isWriteable(obj, prop) {
	    var d = Object.getOwnPropertyDescriptor(obj, prop);
	    return d === undefined || d.writable === true;
	}
	function setProperty(obj, prop, v) {
	    try {
	        obj[prop] = v;
	    }
	    catch (e) {
	    }
	}
	function fetchProperties(context, typ, wsobj, parentUrl) {
	    var links = Meta_1.getLinksMeta(typ).getOrElse(function () { return {}; });
	    var r = new typ();
	    var mergedKeys = Object.keys(flib_1.JsMap.merge([wsobj, links]));
	    var promises = mergedKeys.map(function (k) {
	        var v = wsobj[k];
	        var lnks = links[k];
	        if (flib_1.isNull(v) && !flib_1.isNull(lnks)) {
	            var proms = lnks.map(function (linkOrCnv) {
	                if (isWriteable(r, linkOrCnv.targetProperty)) {
	                    if (linkOrCnv instanceof Converter_1.Converter) {
	                        var errMessage = function (err) { return ("property " + k + " of " + typ.name + " is undefined, error: " + err.message); };
	                        return Util_1.trap(function () { return linkOrCnv.conversion(undefined, objectFetcherImpl(context), flib_1.Option.option(parentUrl)); }, errMessage).then(function (v) {
	                            return setProperty(r, linkOrCnv.targetProperty, v);
	                        });
	                    }
	                    else if (linkOrCnv instanceof Link_1.Link) {
	                        var isOption = linkOrCnv.resultType.kind === TypeExpr_1.TypeExprKind.option || types_1.isOptionType(linkOrCnv.resultType.value);
	                        if (isOption) {
	                            setProperty(r, linkOrCnv.targetProperty, flib_1.Option.none());
	                            return Promise.resolve();
	                        }
	                        else {
	                            return Promise.reject(new Error("link property " + k + " of " + typ.name + " is undefined"));
	                        }
	                    }
	                }
	                else {
	                    return Promise.resolve();
	                }
	            });
	            return (proms.length === 0) ? Promise.resolve() : Promise.all(proms);
	        }
	        else {
	            return flib_1.Option.option(lnks).fold(function () {
	                if (isWriteable(r, k))
	                    setProperty(r, k, v);
	                return Promise.resolve();
	            }, function (lnks) {
	                var proms = lnks.map(function (l) {
	                    if (isWriteable(r, k))
	                        return fetchProperty(context, typ.name, k, v, parentUrl, l, function (v) { setProperty(r, l.targetProperty, v); });
	                    else
	                        return Promise.resolve();
	                });
	                return Promise.all(proms);
	            });
	        }
	    });
	    return Promise.all(promises).then(function (v) { return r; });
	}
	function fetchObject(url, context, resultType, callback) {
	    return fetchInternal(context, resultType).fetch(context.requestFactory(url), callback);
	}
	function fetchUrl(url, context) {
	    return httpFetch(context.requestFactory(url), context.httpCache, context.responseReader);
	}
	function fetchLink(url, context, jsType, callback, propUrl) {
	    var expectingArrayUrl = flib_1.lazy(function () { return Promise.reject(new Error("expecting an array but got " + url)); });
	    var expectingObjectUrl = flib_1.lazy(function () { return Promise.reject(new Error("expecting an string but got " + url)); });
	    return TypeExpr_1.TypeExpr.foldExt(function (resultType) { return typeof url === "string" ? fetchObject(url, context, resultType, callback) : expectingObjectUrl(); }, function (cnv) {
	        if (typeof url === "string") {
	            var prom_1 = fetchUrl(url, context).then(function (v) { return Util_1.trap(function () {
	                var r = cnv.convert(v);
	                callback(r);
	                return r;
	            }); });
	            return withObjectCache(url, TypeExpr_1.TypeExpr.fromMappingType(cnv), function () { return prom_1; }, context, callback);
	        }
	        else {
	            return expectingObjectUrl();
	        }
	    }, function (arr) {
	        if (!Array.isArray(url)) {
	            return expectingArrayUrl();
	        }
	        else {
	            var res_3 = [];
	            var proms = url.map(function (u, idx) {
	                var p1 = fetchLink(u, context, arr.value, function (a) { res_3[idx] = a; }, propUrl);
	                return p1.then(function (i) { return Promise.resolve(i); }, function (err) { return Promise.reject(new Error("error at index " + idx + ", error:" + err.message)); });
	            });
	            return Promise.all(proms).then(function (u) { return callback(res_3); });
	        }
	    }, function (opt) {
	        if (flib_1.isNull(url)) {
	            callback(flib_1.Option.none());
	            return Promise.resolve();
	        }
	        else {
	            return fetchLink(url, context, opt.value, function (a) { return callback(flib_1.Option.some(a)); }, propUrl);
	        }
	    }, function (choose) {
	        if (typeof url === "string") {
	            return fetchUrl(url, context).then(function (wso) {
	                return choose(wso).fold(function () { return Promise.reject(new Error("no choice found for " + wso)); }, function (typ) { return fetchLink(url, context, typ, callback, propUrl); });
	            });
	        }
	        else
	            return expectingObjectUrl();
	    }, function (pg) {
	        callback(pg.convert(flib_1.Option.option(propUrl)));
	        return Promise.resolve();
	    })(jsType);
	}
	function fetchProperty(context, typeName, propertyName, propValue, parentUrl, l, callback) {
	    var res;
	    if (l instanceof Link_1.Link) {
	        if ((typeof propValue === "string") || Array.isArray(propValue)) {
	            res = fetchLink(propValue, context, l.resultType, callback, parentUrl).then(function (i) { return Promise.resolve(i); }, function (err) {
	                var msg = "error fetching property  " + propertyName + " of " + typeName + " as link: " + err.message;
	                return Promise.reject(new Error(msg));
	            });
	        }
	        else {
	            res = Promise.reject(new Error("error fetching property  " + propertyName + " of " + typeName + " expecting a link got " + propValue));
	        }
	    }
	    else if (l instanceof Converter_1.Converter) {
	        res = l.conversion(propValue, objectFetcherImpl(context), flib_1.Option.option(parentUrl)).then(function (i) {
	            callback(i);
	            return Promise.resolve(i);
	        }, function (err) {
	            var msg = "error applying conversion of property " + propertyName + " of " + typeName + ": " + err.message;
	            return Promise.reject(new Error(msg));
	        });
	    }
	    return res;
	}
	function withObjectCache(url, typ, f, context, callback) {
	    function storeCache(url, cache, typ, p) {
	        cache.store(url, typ, p);
	        return p;
	    }
	    return context.cache.get(url, typ).fold(function () { return storeCache(url, context.cache, typ, f()); }, function (cacheValue) {
	        context.promisesToWait.push(cacheValue.then(function (v) {
	            callback(v);
	            return v;
	        }));
	        return Promise.resolve();
	    });
	}
	function fetchInternal(context, typ) {
	    if (flib_1.isNull(typ))
	        throw new Error("fetchInternal: undefined type");
	    var linksMeta = typ && Meta_1.getLinksMeta(typ);
	    var typExpr = TypeExpr_1.TypeExpr.fromMappingType(typ);
	    return {
	        fetch: function (req, callback) {
	            var prom = function () { return httpFetch(req, context.httpCache, context.responseReader).then(function (a) {
	                return linksMeta.fold(function () {
	                    var r = new typ();
	                    Object.keys(a).forEach(function (k) {
	                        if (isWriteable(r, k))
	                            setProperty(r, k, a[k]);
	                    });
	                    callback(r);
	                    return r;
	                }, function (linksMeta) { return fetchProperties(context, typ, a, req.url).then(function (u) {
	                    callback(u);
	                    return u;
	                }); });
	            }); };
	            return withObjectCache(req.url, typExpr, prom, context, callback);
	        }
	    };
	}


/***/ },
/* 27 */
/***/ function(module, exports) {

	"use strict";
	function parseJson(txt) {
	    try {
	        return Promise.resolve(JSON.parse(txt));
	    }
	    catch (e) {
	        return Promise.reject(new Error("Error parsing json " + e.message + " json:\n" + txt));
	    }
	}
	function statusOk(resp) {
	    var st = resp.status;
	    return (st >= 200 && st < 300);
	}
	function reqDesc(req) {
	    return req.method + " " + req.url;
	}
	exports.jsonResponseReader = function (resp, req) {
	    if (statusOk(resp)) {
	        return resp.text().then(parseJson);
	    }
	    else {
	        return Promise.reject(new Error(reqDesc(req) + " return status : " + resp.status));
	    }
	};


/***/ },
/* 28 */
/***/ function(module, exports) {

	"use strict";
	function createRequestFactory(reqInit) {
	    return function (url) {
	        return new Request(url, reqInit);
	    };
	}
	exports.createRequestFactory = createRequestFactory;


/***/ },
/* 29 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var ByUrlCache = (function () {
	    function ByUrlCache() {
	        this.cache = {};
	    }
	    ByUrlCache.prototype.get = function (req) {
	        return flib_1.Option.option(this.cache[req.url]);
	    };
	    ByUrlCache.prototype.store = function (req, promise) {
	        this.cache[req.url] = promise;
	    };
	    return ByUrlCache;
	}());
	exports.ByUrlCache = ByUrlCache;
	exports.noCache = {
	    get: function (req) {
	        return flib_1.Option.none();
	    },
	    store: function (req, promise) {
	    }
	};


/***/ },
/* 30 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var Meta_1 = __webpack_require__(24);
	var types_1 = __webpack_require__(20);
	var InternalConversion_1 = __webpack_require__(31);
	var SimpleConverter_1 = __webpack_require__(21);
	var Converter = (function () {
	    function Converter(conversion, targetProperty, resultType) {
	        this.conversion = conversion;
	        this.targetProperty = targetProperty;
	        this.resultType = resultType;
	    }
	    return Converter;
	}());
	exports.Converter = Converter;
	function createConverter(conversion, targetProperty, resultType) {
	    return new Converter(InternalConversion_1.jsTypeToInternalConversion(conversion), targetProperty, resultType);
	}
	function getPrimitiveSimpleConverter(ct) {
	    return SimpleConverter_1.SimpleConverter["from" + ct.name];
	}
	function convert(ptyp, opts) {
	    return function (target, key) {
	        var propType = Reflect.getMetadata("design:type", target, key);
	        var typ = !flib_1.isNull(ptyp) && types_1.MappingType.isMappingType(ptyp) ?
	            ptyp :
	            (flib_1.isNull(propType) ? SimpleConverter_1.SimpleConverter.identity : (types_1.isPrimitiveType(propType) ? getPrimitiveSimpleConverter(propType) : propType));
	        var objLinks = Meta_1.getOrCreateLinksMeta(target.constructor);
	        var targetProperty = (opts && opts.property) || (ptyp && ptyp["property"]) || key;
	        Meta_1.addObjectMapping(objLinks, targetProperty, createConverter(typ, key, propType));
	    };
	}
	exports.convert = convert;
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
	exports.propertyUrl = new types_1.GetPropertyUrl(function (id) { return id.getOrElse(function () { return flib_1.fail("missing property url"); }); });
	exports.optionalPropertyUrl = new types_1.GetPropertyUrl(function (id) { return id; });


/***/ },
/* 31 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var types_1 = __webpack_require__(20);
	var flib_1 = __webpack_require__(5);
	var Util_1 = __webpack_require__(32);
	function failedConversion(msg) {
	    return function () {
	        return Promise.reject(new Error(msg));
	    };
	}
	exports.jsTypeToInternalConversion = types_1.MappingType.fold(function (ct) { return function (a, objectFetcher, parentUrl) {
	    return objectFetcher(ct, a);
	}; }, function (s) { return function (a, objectFetcher, parentUrl) {
	    return Util_1.trap(function () { return s.convert(a); });
	}; }, function (arr) {
	    var itmCnv = exports.jsTypeToInternalConversion(arr.arrayOf);
	    return function (a, objectFetcher, parentUrl) {
	        if (!Array.isArray(a))
	            return Promise.reject(new Error("invalid array ${a}"));
	        else
	            return Promise.all(a.map(function (i) { return itmCnv(i, objectFetcher, parentUrl); }));
	    };
	}, function (opts) { return function (a, objectFetcher, parentUrl) {
	    if (flib_1.isNull(a))
	        return Promise.resolve(flib_1.Option.none());
	    else
	        return objectFetcher(Object, a).then(function (obj) { return flib_1.Option.some(obj); });
	}; }, function (opts) { return function (a, objectFetcher, parentUrl) {
	    return objectFetcher(Object, a).then(function (obj) {
	        var cnv = opts.convert(obj).map(function (t) {
	            return exports.jsTypeToInternalConversion(t);
	        }).getOrElse(function () { return failedConversion("invalid " + (flib_1.isNull(opts.description) ? "choice" : opts.description)); });
	        return cnv(a, objectFetcher, parentUrl);
	    });
	}; }, function (purl) { return function (a, ftcr, parUrl) {
	    return Util_1.trap(function () { return purl.convert(parUrl); });
	}; });


/***/ },
/* 32 */
/***/ function(module, exports) {

	"use strict";
	function trap(f, errorDescription) {
	    try {
	        var r = f();
	        if (r instanceof Promise)
	            return r;
	        else
	            return Promise.resolve(r);
	    }
	    catch (err) {
	        var desc = typeof errorDescription === "function" ? errorDescription(err) : errorDescription;
	        return Promise.reject(new Error("" + (desc || err.message)));
	    }
	}
	exports.trap = trap;


/***/ },
/* 33 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var EntriesMap_1 = __webpack_require__(25);
	var ObjectsCache = (function (_super) {
	    __extends(ObjectsCache, _super);
	    function ObjectsCache() {
	        _super.call(this, function (a, b) { return a.equalTo(b); });
	    }
	    return ObjectsCache;
	}(EntriesMap_1.EntriesMap));
	exports.ObjectsCache = ObjectsCache;


/***/ },
/* 34 */
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
	var model_1 = __webpack_require__(17);
	var rest_fetch_1 = __webpack_require__(18);
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
	        rest_fetch_1.link({ arrayOf: Player }), 
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
	        rest_fetch_1.link(), 
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
	        rest_fetch_1.link(), 
	        __metadata('design:type', Player)
	    ], PlayerLogOff.prototype, "player", void 0);
	    return PlayerLogOff;
	}());
	exports.PlayerLogOff = PlayerLogOff;
	exports.playerEventChoice = model_1.byKindChoice(function () {
	    return [{
	            key: PlayerEventKind[PlayerEventKind.playerLogOn],
	            value: PlayerLogOn
	        }, {
	            key: PlayerEventKind[PlayerEventKind.playerLogOff],
	            value: PlayerLogOff
	        }];
	}, "player event");
	var PlayersCollection = (function () {
	    function PlayersCollection() {
	    }
	    __decorate([
	        rest_fetch_1.link({ arrayOf: Player }), 
	        __metadata('design:type', Array)
	    ], PlayersCollection.prototype, "members", void 0);
	    return PlayersCollection;
	}());
	exports.PlayersCollection = PlayersCollection;


/***/ },
/* 35 */
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
	var competition_1 = __webpack_require__(16);
	var model_1 = __webpack_require__(17);
	var player_1 = __webpack_require__(34);
	var rest_fetch_1 = __webpack_require__(18);
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
	        rest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], CompetitionDeclined.prototype, "player", void 0);
	    __decorate([
	        rest_fetch_1.link(), 
	        __metadata('design:type', competition_1.CompetitionState)
	    ], CompetitionDeclined.prototype, "competition", void 0);
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
	        rest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], CompetitionAccepted.prototype, "player", void 0);
	    __decorate([
	        rest_fetch_1.link(), 
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
	        rest_fetch_1.link(), 
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
	        rest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], CreatedCompetition.prototype, "issuer", void 0);
	    __decorate([
	        rest_fetch_1.link(), 
	        __metadata('design:type', competition_1.CompetitionState)
	    ], CreatedCompetition.prototype, "competition", void 0);
	    return CreatedCompetition;
	}());
	exports.CreatedCompetition = CreatedCompetition;
	exports.competitionEventChoice = model_1.byKindChoice(function () { return [{
	        key: CompetitionEventKind[CompetitionEventKind.createdCompetition],
	        value: CreatedCompetition
	    }, {
	        key: CompetitionEventKind[CompetitionEventKind.confirmedCompetition],
	        value: ConfirmedCompetition
	    }, {
	        key: CompetitionEventKind[CompetitionEventKind.playerAccepted],
	        value: CompetitionAccepted
	    }, {
	        key: CompetitionEventKind[CompetitionEventKind.playerDeclined],
	        value: CompetitionDeclined
	    }]; }, "competition event");


/***/ },
/* 36 */
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
	var model_1 = __webpack_require__(17);
	var player_1 = __webpack_require__(34);
	var rest_fetch_1 = __webpack_require__(18);
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
	var stringToSeed = rest_fetch_1.SimpleConverter.fromString.andThen(rest_fetch_1.Converter.toEnum(Seed, "Seed"));
	var Card = (function () {
	    function Card() {
	    }
	    __decorate([
	        rest_fetch_1.convert(stringToSeed), 
	        __metadata('design:type', Number)
	    ], Card.prototype, "seed", void 0);
	    return Card;
	}());
	exports.Card = Card;
	var Move = (function () {
	    function Move() {
	    }
	    __decorate([
	        rest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], Move.prototype, "player", void 0);
	    __decorate([
	        rest_fetch_1.convert(), 
	        __metadata('design:type', Card)
	    ], Move.prototype, "card", void 0);
	    return Move;
	}());
	exports.Move = Move;
	var PlayerScore = (function () {
	    function PlayerScore() {
	    }
	    __decorate([
	        rest_fetch_1.convert({ arrayOf: Card }), 
	        __metadata('design:type', Array)
	    ], PlayerScore.prototype, "cards", void 0);
	    return PlayerScore;
	}());
	exports.PlayerScore = PlayerScore;
	var PlayerFinalState = (function () {
	    function PlayerFinalState() {
	    }
	    __decorate([
	        rest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], PlayerFinalState.prototype, "player", void 0);
	    __decorate([
	        rest_fetch_1.convert(), 
	        __metadata('design:type', PlayerScore)
	    ], PlayerFinalState.prototype, "score", void 0);
	    return PlayerFinalState;
	}());
	exports.PlayerFinalState = PlayerFinalState;
	var PlayerState = (function () {
	    function PlayerState() {
	    }
	    __decorate([
	        rest_fetch_1.convert(rest_fetch_1.Converter.propertyUrl), 
	        __metadata('design:type', String)
	    ], PlayerState.prototype, "self", void 0);
	    __decorate([
	        rest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], PlayerState.prototype, "player", void 0);
	    __decorate([
	        rest_fetch_1.convert({ arrayOf: Card }), 
	        __metadata('design:type', Array)
	    ], PlayerState.prototype, "cards", void 0);
	    __decorate([
	        rest_fetch_1.convert(), 
	        __metadata('design:type', PlayerScore)
	    ], PlayerState.prototype, "score", void 0);
	    return PlayerState;
	}());
	exports.PlayerState = PlayerState;
	var PlayersGameResult = (function () {
	    function PlayersGameResult() {
	    }
	    __decorate([
	        rest_fetch_1.convert({ arrayOf: PlayerFinalState }), 
	        __metadata('design:type', Array)
	    ], PlayersGameResult.prototype, "playersOrderByPoints", void 0);
	    __decorate([
	        rest_fetch_1.convert(), 
	        __metadata('design:type', PlayerFinalState)
	    ], PlayersGameResult.prototype, "winner", void 0);
	    return PlayersGameResult;
	}());
	exports.PlayersGameResult = PlayersGameResult;
	var TeamScore = (function () {
	    function TeamScore() {
	    }
	    __decorate([
	        rest_fetch_1.link({ arrayOf: player_1.Player }), 
	        __metadata('design:type', Array)
	    ], TeamScore.prototype, "players", void 0);
	    __decorate([
	        rest_fetch_1.convert({ arrayOf: Card }), 
	        __metadata('design:type', Array)
	    ], TeamScore.prototype, "cards", void 0);
	    return TeamScore;
	}());
	exports.TeamScore = TeamScore;
	var TeamsGameResult = (function () {
	    function TeamsGameResult() {
	    }
	    __decorate([
	        rest_fetch_1.convert({ arrayOf: TeamScore }), 
	        __metadata('design:type', Array)
	    ], TeamsGameResult.prototype, "teamsOrderByPoints", void 0);
	    __decorate([
	        rest_fetch_1.convert(), 
	        __metadata('design:type', TeamScore)
	    ], TeamsGameResult.prototype, "winnerTeam", void 0);
	    return TeamsGameResult;
	}());
	exports.TeamsGameResult = TeamsGameResult;
	var gameResultChoice = rest_fetch_1.Selector.byPropertyExists([{ key: "playersOrderByPoints", value: PlayersGameResult }, { key: "teamsOrderByPoints", value: TeamsGameResult }]);
	var FinalGameState = (function () {
	    function FinalGameState() {
	    }
	    __decorate([
	        rest_fetch_1.convert(), 
	        __metadata('design:type', Card)
	    ], FinalGameState.prototype, "briscolaCard", void 0);
	    __decorate([
	        rest_fetch_1.convert(gameResultChoice), 
	        __metadata('design:type', Object)
	    ], FinalGameState.prototype, "gameResult", void 0);
	    return FinalGameState;
	}());
	exports.FinalGameState = FinalGameState;
	var ActiveGameState = (function () {
	    function ActiveGameState() {
	    }
	    __decorate([
	        rest_fetch_1.convert(), 
	        __metadata('design:type', Card)
	    ], ActiveGameState.prototype, "briscolaCard", void 0);
	    __decorate([
	        rest_fetch_1.convert({ arrayOf: Move }), 
	        __metadata('design:type', Array)
	    ], ActiveGameState.prototype, "moves", void 0);
	    __decorate([
	        rest_fetch_1.link({ arrayOf: player_1.Player }), 
	        __metadata('design:type', Array)
	    ], ActiveGameState.prototype, "nextPlayers", void 0);
	    __decorate([
	        rest_fetch_1.link(), 
	        __metadata('design:type', player_1.CurrentPlayer)
	    ], ActiveGameState.prototype, "currentPlayer", void 0);
	    __decorate([
	        rest_fetch_1.link({ arrayOf: player_1.Player }), 
	        __metadata('design:type', Array)
	    ], ActiveGameState.prototype, "players", void 0);
	    __decorate([
	        rest_fetch_1.link({ optionOf: PlayerState }), 
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
	exports.dropReasonChoice = new rest_fetch_1.Selector(function (ws) { return flib_1.Option.some(PlayerLeft); });
	var PlayerLeft = (function (_super) {
	    __extends(PlayerLeft, _super);
	    function PlayerLeft() {
	        _super.apply(this, arguments);
	    }
	    __decorate([
	        rest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], PlayerLeft.prototype, "player", void 0);
	    __decorate([
	        rest_fetch_1.convert(rest_fetch_1.SimpleConverter.optional(rest_fetch_1.SimpleConverter.fromString)), 
	        __metadata('design:type', flib_1.Option)
	    ], PlayerLeft.prototype, "reason", void 0);
	    return PlayerLeft;
	}(DropReason));
	exports.PlayerLeft = PlayerLeft;
	var DroppedGameState = (function () {
	    function DroppedGameState() {
	    }
	    __decorate([
	        rest_fetch_1.link({ arrayOf: player_1.Player }), 
	        __metadata('design:type', Array)
	    ], DroppedGameState.prototype, "nextPlayers", void 0);
	    __decorate([
	        rest_fetch_1.convert(exports.dropReasonChoice), 
	        __metadata('design:type', DropReason)
	    ], DroppedGameState.prototype, "dropReason", void 0);
	    return DroppedGameState;
	}());
	exports.DroppedGameState = DroppedGameState;
	exports.gameStateChoice = model_1.byKindChoice(function () { return [{
	        key: GameStateKind[GameStateKind.active],
	        value: ActiveGameState
	    }, {
	        key: GameStateKind[GameStateKind.dropped],
	        value: DroppedGameState
	    }, {
	        key: GameStateKind[GameStateKind.finished],
	        value: FinalGameState
	    }]; }, "game state choice");
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
/* 37 */
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
	var game_1 = __webpack_require__(36);
	var rest_fetch_1 = __webpack_require__(18);
	var model_1 = __webpack_require__(17);
	var player_1 = __webpack_require__(34);
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
	        rest_fetch_1.link(game_1.gameStateChoice), 
	        __metadata('design:type', Object)
	    ], CardPlayed.prototype, "game", void 0);
	    __decorate([
	        rest_fetch_1.link(), 
	        __metadata('design:type', player_1.Player)
	    ], CardPlayed.prototype, "player", void 0);
	    __decorate([
	        rest_fetch_1.convert(), 
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
	        rest_fetch_1.convert(game_1.ActiveGameState), 
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
	        rest_fetch_1.link(game_1.gameStateChoice), 
	        __metadata('design:type', Object)
	    ], GameDropped.prototype, "game", void 0);
	    __decorate([
	        rest_fetch_1.convert(game_1.dropReasonChoice), 
	        __metadata('design:type', game_1.DropReason)
	    ], GameDropped.prototype, "dropReason", void 0);
	    return GameDropped;
	}());
	exports.GameDropped = GameDropped;
	exports.briscolaEventChoice = model_1.byKindChoice(function () {
	    return [{
	            key: BriscolaEventKind[BriscolaEventKind.gameStarted],
	            value: GameStarted
	        }, {
	            key: BriscolaEventKind[BriscolaEventKind.cardPlayed],
	            value: CardPlayed
	        }, {
	            key: BriscolaEventKind[BriscolaEventKind.gameDropped],
	            value: GameDropped
	        }];
	}, "game event");


/***/ },
/* 38 */
/***/ function(module, exports) {

	"use strict";
	var SiteMap = (function () {
	    function SiteMap() {
	    }
	    return SiteMap;
	}());
	exports.SiteMap = SiteMap;


/***/ },
/* 39 */
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
	var game_1 = __webpack_require__(36);
	var gameEvents_1 = __webpack_require__(37);
	var competition_1 = __webpack_require__(16);
	var competitionEvents_1 = __webpack_require__(35);
	var player_1 = __webpack_require__(34);
	var rest_fetch_1 = __webpack_require__(18);
	/*
	export type RawGame = { kind:string }
	export type RawCompetition = { kind:string }
	
	export function byEventNameChoice<T>(desc:string, mp:JsMap<ConstructorType<any>>):(wso:RawEvent) => Option<ConstructorType<T>> {
	  return (wso) => Option.option(mp[wso.kind])
	}
	
	const playerEventChoiceMap = JsMap.create(playerEventChoices())
	
	// FIXME  make private
	export const gameEventChoiceMap = JsMap.create(briscolaEventChoices())
	// FIXME  make private
	export const gameStateChoiceMap = JsMap.create(gameStateChoices())
	const competitionEventChoiceMap = JsMap.create(competitionEventChoices())
	
	export const playerEventChoice = Selector.create( byEventNameChoice("player event", playerEventChoiceMap) )
	export const gameEventChoice = Selector.create( byEventNameChoice("game event", gameEventChoiceMap) )
	export const competitionEventChoice = Selector.create( byEventNameChoice("competition event", competitionEventChoiceMap) )
	
	export const gameStateChoice = Selector.create(
	  (gm:RawGame) => Option.option(gameStateChoiceMap[gm.kind])
	)
	*/
	exports.eventAndStateChoice = rest_fetch_1.Selector.create(function (wso) {
	    var evName = wso.event && wso.event.kind;
	    if (player_1.playerEventChoice.contains(evName))
	        return flib_1.Option.some(PlayerEventAndState);
	    else if (gameEvents_1.briscolaEventChoice.contains(evName))
	        return flib_1.Option.some(GameEventAndState);
	    else if (competitionEvents_1.competitionEventChoice.contains(evName))
	        return flib_1.Option.some(CompetitionEventAndState);
	    return flib_1.Option.none();
	});
	var PlayerEventAndState = (function () {
	    function PlayerEventAndState() {
	    }
	    __decorate([
	        rest_fetch_1.convert(player_1.playerEventChoice), 
	        __metadata('design:type', Object)
	    ], PlayerEventAndState.prototype, "event", void 0);
	    __decorate([
	        rest_fetch_1.convert({ arrayOf: player_1.Player }, { property: "state" }), 
	        __metadata('design:type', Array)
	    ], PlayerEventAndState.prototype, "players", void 0);
	    return PlayerEventAndState;
	}());
	exports.PlayerEventAndState = PlayerEventAndState;
	var GameEventAndState = (function () {
	    function GameEventAndState() {
	    }
	    __decorate([
	        rest_fetch_1.convert(gameEvents_1.briscolaEventChoice), 
	        __metadata('design:type', Object)
	    ], GameEventAndState.prototype, "event", void 0);
	    __decorate([
	        rest_fetch_1.convert(game_1.gameStateChoice, { property: "state" }), 
	        __metadata('design:type', Object)
	    ], GameEventAndState.prototype, "game", void 0);
	    return GameEventAndState;
	}());
	exports.GameEventAndState = GameEventAndState;
	var CompetitionEventAndState = (function () {
	    function CompetitionEventAndState() {
	    }
	    __decorate([
	        rest_fetch_1.convert(competitionEvents_1.competitionEventChoice), 
	        __metadata('design:type', Object)
	    ], CompetitionEventAndState.prototype, "event", void 0);
	    __decorate([
	        rest_fetch_1.convert(competition_1.CompetitionState, { property: "state" }), 
	        __metadata('design:type', competition_1.CompetitionState)
	    ], CompetitionEventAndState.prototype, "competition", void 0);
	    return CompetitionEventAndState;
	}());
	exports.CompetitionEventAndState = CompetitionEventAndState;


/***/ },
/* 40 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var game_1 = __webpack_require__(36);
	function card(m) {
	    return {
	        number: m.number,
	        seed: game_1.Seed[m.seed]
	    };
	}
	exports.card = card;


/***/ },
/* 41 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var flib_1 = __webpack_require__(5);
	var Http;
	(function (Http) {
	    function fetchWithBody(mthd) {
	        return function (url, body) {
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
	function observableWebSocket1(url, protocol) {
	    var channel = new Rx.Subject();
	    var socket = protocol === undefined ? new WebSocket(url, protocol) : new WebSocket(url);
	    function showEvent(e) {
	        console.log("receive from websocket");
	        console.log(e);
	    }
	    socket.onclose = function (e) {
	        showEvent(e);
	        channel.onCompleted();
	    };
	    socket.onerror = function (e) {
	        showEvent(e);
	        channel.onError(e);
	    };
	    socket.onopen = showEvent;
	    socket.onmessage = function (e) {
	        showEvent(e);
	        channel.onNext(e);
	    };
	    return channel;
	}
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


/***/ },
/* 42 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var Util_1 = __webpack_require__(41);
	var Util = __webpack_require__(41);
	var flib_1 = __webpack_require__(5);
	var ddd_briscola_model_1 = __webpack_require__(14);
	var rest_fetch_1 = __webpack_require__(18);
	function gamesMap(ch) {
	    var mapOfGames = {};
	    var feedGamesMap = function (gm) {
	        if (gm instanceof ddd_briscola_model_1.ActiveGameState) {
	            mapOfGames[gm.self] = gm;
	        }
	        else if (gm instanceof ddd_briscola_model_1.FinalGameState) {
	            mapOfGames[gm.self] = gm;
	        }
	    };
	    ch.subscribe(function (es) { return feedGamesMap(es.game); });
	    return function (p) { return flib_1.Option.option(mapOfGames[p]); };
	}
	function competitionsMap(ch) {
	    var compMap = {};
	    ch.subscribe(function (es) {
	        var compState = es.competition;
	        if (es.competition.kind === ddd_briscola_model_1.CompetitionStateKind.open) {
	            compMap[compState.self] = compState;
	        }
	        else {
	            delete compMap[compState.self];
	        }
	    });
	    return function (p) { return flib_1.Option.option(compMap[p]); };
	}
	function asGameEventAndState(a) {
	    if (a instanceof ddd_briscola_model_1.GameEventAndState)
	        return flib_1.Option.some(a);
	    else
	        return flib_1.Option.none();
	}
	function asCompetitionEventAndState(a) {
	    if (a instanceof ddd_briscola_model_1.CompetitionEventAndState)
	        return flib_1.Option.some(a);
	    else
	        return flib_1.Option.none();
	}
	function asPlayerEventAndState(a) {
	    if (a instanceof ddd_briscola_model_1.PlayerEventAndState)
	        return flib_1.Option.some(a);
	    else
	        return flib_1.Option.none();
	}
	var PlayerService = (function () {
	    function PlayerService(player) {
	        this.player = player;
	        var webSocket = Util_1.observableWebSocket(player.webSocket).flatMap(function (msgEv) {
	            var data = msgEv.data;
	            if (typeof data === "string") {
	                var msg = JSON.parse(data);
	                return rest_fetch_1.fetchChoose(ddd_briscola_model_1.eventAndStateChoice).fromObject(msg).then(function (v) { return Promise.resolve(v); }, function (err) {
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
	        this.gamesMap = gamesMap(this.gamesChannel);
	        this.competitionsMap = competitionsMap(this.competitionsChannel);
	    }
	    PlayerService.prototype.createCompetition = function (players, kind, deadlineKind) {
	        return Util.Http.POST(this.player.createCompetition, {
	            players: players,
	            kind: kind,
	            deadline: deadlineKind
	        }).then(function (p) { return rest_fetch_1.fetch(ddd_briscola_model_1.CompetitionState).fromObject(p); });
	    };
	    PlayerService.prototype.gameChannelById = function (gameSelf) {
	        return this.gamesChannel.filter(function (es) {
	            return (es.game instanceof ddd_briscola_model_1.ActiveGameState && es.game.self === gameSelf) ||
	                (es.game instanceof ddd_briscola_model_1.FinalGameState && es.game.self === gameSelf);
	        });
	    };
	    PlayerService.prototype.playCard = function (gameSelf, mcard) {
	        function playerStateUrl(gm) {
	            if (gm instanceof ddd_briscola_model_1.ActiveGameState) {
	                return gm.playerState.map(function (ps) { return ps.self; });
	            }
	            else {
	                return flib_1.Option.none();
	            }
	        }
	        var card = ddd_briscola_model_1.Input.card(mcard);
	        return this.gamesMap(gameSelf).flatMap(function (gm) {
	            var url = playerStateUrl(gm);
	            return url.map(function (url) {
	                return Util.Http.POST(url, {
	                    "number": card.number,
	                    seed: card.seed
	                }).then(function (resp) {
	                    return resp.json().then(function (ws) {
	                        return rest_fetch_1.fetchChoose(ddd_briscola_model_1.gameStateChoice).fromObject(ws);
	                    });
	                });
	            });
	        });
	    };
	    PlayerService.prototype.acceptCompetition = function (compSelf) {
	        return this.competitionsMap(compSelf).flatMap(function (cs) {
	            return cs.accept.map(function (url) {
	                return Util.Http.POST(url).then(function (resp) {
	                    return resp.json().then(function (ws) { return rest_fetch_1.fetch(ddd_briscola_model_1.CompetitionState).fromObject(ws); });
	                });
	            });
	        });
	    };
	    PlayerService.prototype.declineCompetition = function (compSelf) {
	        return this.competitionsMap(compSelf).flatMap(function (cs) {
	            return cs.decline.map(function (url) {
	                return Util.Http.POST(url).then(function (resp) {
	                    return resp.json().then(function (ws) {
	                        return rest_fetch_1.fetch(ddd_briscola_model_1.CompetitionState).fromObject(ws);
	                    });
	                });
	            });
	        });
	    };
	    return PlayerService;
	}());
	exports.PlayerService = PlayerService;


/***/ },
/* 43 */
/***/ function(module, exports) {

	"use strict";
	var CommandDispatcher = (function () {
	    function CommandDispatcher(select, initialState) {
	        this.select = select;
	        this.changesChannel = new Rx.ReplaySubject();
	        this.currentState = initialState;
	    }
	    CommandDispatcher.prototype.dispatch = function (command) {
	        var _this = this;
	        return this.select(command)(this.currentState, command, function (cmd) { return _this.dispatch(cmd); }).then(function (newState) {
	            _this.currentState = newState;
	            _this.changesChannel.onNext(newState);
	            return newState;
	        });
	    };
	    CommandDispatcher.prototype.changes = function () {
	        return this.changesChannel;
	    };
	    return CommandDispatcher;
	}());
	Object.defineProperty(exports, "__esModule", { value: true });
	exports.default = CommandDispatcher;


/***/ },
/* 44 */
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
/* 45 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var flib_1 = __webpack_require__(5);
	var Model = __webpack_require__(14);
	var cssClasses_1 = __webpack_require__(46);
	var cards_1 = __webpack_require__(47);
	var EventsLog_1 = __webpack_require__(48);
	var GameResult_1 = __webpack_require__(49);
	var PlayerDeck_1 = __webpack_require__(51);
	var PlayerLogin_1 = __webpack_require__(52);
	var Players_1 = __webpack_require__(53);
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
	            return React.createElement("div", {key: idx}, React.createElement(cards_1.Card, {card: mv.card}), React.createElement("span", null, mv.player.name));
	        });
	        elemEvs = elemEvs.concat(gm.nextPlayers.map(function (p, idx) {
	            return React.createElement("div", {key: idx + gm.moves.length}, React.createElement(cards_1.EmptyCard, null), React.createElement("span", null, p.name));
	        }));
	        elemEvs.push(React.createElement("div", {key: gm.nextPlayers.length + gm.moves.length + 1}, React.createElement(cards_1.Card, {card: gm.briscolaCard, classes: [cssClasses_1.default.halfCard]}), React.createElement(cards_1.CardBack, {classes: [cssClasses_1.default.halfCard]}), React.createElement("span", null, gm.deckCardsNumber)));
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
	            return ps.cards.map(function (card, idx) { return React.createElement("div", {key: idx}, React.createElement(cards_1.Card, {card: card, onClick: function () { return props.onPlayCard(card); }})); });
	        }).getOrElse(function () { return [React.createElement("span", {key: "0"}, "Player has no cards")]; });
	        return (React.createElement("div", {className: cssClasses_1.default.cards}, elems, React.createElement("div", null, React.createElement(cards_1.CardBack, {onClick: function () { return props.onPlayerDeck(gameId, true); }}))));
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
	            return React.createElement("section", null, React.createElement(GameTable, {game: gm}), React.createElement(PlayerCards, {game: gm, onPlayerDeck: props.onPlayerDeck, onPlayCard: props.onPlayCard}));
	        }, function (gm) {
	            return React.createElement("section", null, React.createElement(GameResult_1.GameResult, {game: gm}));
	        }, function (gm) {
	            return React.createElement("section", null, React.createElement("h1", null, "Dropped Game !!!"));
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
	            return React.createElement("section", null, React.createElement(Game, {game: gm, onPlayerDeck: props.onPlayerDeck, onPlayCard: props.onPlayCard}), React.createElement(GamesNav, {current: gm.self, games: Object.keys(board.activeGames), onSelectedGame: props.onSelectedGame}), React.createElement(GamesNav, {current: gm.self, games: Object.keys(board.finishedGames), onSelectedGame: props.onSelectedGame}));
	        }).getOrElse(function () { return React.createElement("noscript", null); });
	        var playerDeckDialog = board.viewFlag === Model.ViewFlag.showPlayerCards ?
	            board.currentGame.map(function (gm) {
	                return Model.GameState.fold(gm, function (gm) { return React.createElement(PlayerDeck_1.PlayerDeckSummary, {cards: gm.playerState.map(function (ps) { return ps.score.cards; }).getOrElse(function () { return []; }), onClose: function () { return props.onPlayerDeck(gm.self, false); }}); }, function (gm) { return React.createElement("noscript", null); }, function (gm) { return React.createElement("noscript", null); });
	            }).getOrElse(function () { return React.createElement("noscript", null); }) : React.createElement("noscript", null);
	        var startCompetitionButton = showCompetitionButton(board) ? React.createElement(StartCompetition, {onStartCompetition: props.onStartCompetition}) : React.createElement("noscript", null);
	        return board.player.map(function (pl) { return (React.createElement("div", null, playerDeckDialog, React.createElement(Players_1.CurrentPlayer, React.__spread({}, pl)), React.createElement(Players_1.Players, {players: board.players, selectedPlayers: board.competitionSelectedPlayers, onPlayerSelection: props.onPlayerSelection}), startCompetitionButton, gameSection, React.createElement(EventsLog_1.EventsLog, {events: board.eventsLog, onAcceptCompetition: props.onAcceptCompetition, onDeclineCompetition: props.onDeclineCompetition, onSelectedGame: props.onSelectedGame}))); }).getOrElse(function () { return (React.createElement(PlayerLogin_1.PlayerLogin, {onCreatePlayer: props.onCreatePlayer, onPlayerLogin: props.onPlayerLogin})); });
	    };
	    return Board;
	}(React.Component));
	exports.Board = Board;


/***/ },
/* 46 */
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
/* 47 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var Model = __webpack_require__(14);
	var cssClasses_1 = __webpack_require__(46);
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
	        return (React.createElement("img", React.__spread({}, extra, {className: className, onClick: onClick})));
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
/* 48 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var Model = __webpack_require__(14);
	var PlayerEventKind = Model.PlayerEventKind;
	var BriscolaEventKind = Model.BriscolaEventKind;
	var CompetitionEventKind = Model.CompetitionEventKind;
	var cssClasses_1 = __webpack_require__(46);
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
	                    return (React.createElement("div", {key: i}, "Player ", React.createElement("b", null, ev.player.name), " log on"));
	                }
	                case PlayerEventKind[PlayerEventKind.playerLogOff]: {
	                    var ev = event;
	                    return (React.createElement("div", {key: i}, "Player ", React.createElement("b", null, ev.player.name), " log off"));
	                }
	                case CompetitionEventKind[CompetitionEventKind.createdCompetition]: {
	                    var ev_1 = event;
	                    return (React.createElement("div", {key: i}, React.createElement("p", null, "Player ", ev_1.issuer.name, " invited you !!!"), React.createElement("p", null, "players are ", ev_1.competition.competition.players.map(function (pl) { return React.createElement("b", null, pl.name, " "); }), " of kind ", React.createElement("b", null, Model.MatchKindKind[ev_1.competition.competition.kind.kind]), " "), React.createElement("p", null, ev_1.competition.accept.map(function (url) {
	                        return React.createElement("button", {onClick: function (clkEv) { return props.onAcceptCompetition(ev_1.competition); }, key: "1"}, "Accept competiton");
	                    }).getOrElse(function () { return React.createElement("noscript", null); }), ev_1.competition.decline.map(function (url) {
	                        return React.createElement("button", {onClick: function (clkEv) { return props.onDeclineCompetition(ev_1.competition); }, key: "1"}, "Decline competiton");
	                    }).getOrElse(function () { return React.createElement("noscript", null); }))));
	                }
	                case BriscolaEventKind[BriscolaEventKind.gameStarted]: {
	                    var ev_2 = event;
	                    return (React.createElement("div", {key: i}, React.createElement("a", {href: "#", onClick: function (cev) { return props.onSelectedGame(ev_2.game.self); }}, "A game has started")));
	                }
	                case BriscolaEventKind[BriscolaEventKind.cardPlayed]: {
	                    var ev_3 = event;
	                    return (React.createElement("div", {key: i}, React.createElement("a", {href: "#", onClick: function (cev) { return props.onSelectedGame(ev_3.game.self); }}, ev_3.player.name, " played ", ev_3.card.number, " ", Model.Seed[ev_3.card.seed])));
	                }
	                default: {
	                    return (React.createElement("div", {key: i}, React.createElement("textarea", {rows: 4, cols: 80, defaultValue: JSON.stringify(event)})));
	                }
	            }
	        });
	        return (React.createElement("div", {className: cssClasses_1.default.eventLog}, elemEvs));
	    };
	    return EventsLog;
	}(React.Component));
	exports.EventsLog = EventsLog;


/***/ },
/* 49 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var Model = __webpack_require__(14);
	var cssClasses_1 = __webpack_require__(46);
	var TabPane_1 = __webpack_require__(50);
	var PlayerDeck_1 = __webpack_require__(51);
	function playersGameResultTabPaneItems(gameRes) {
	    return gameRes.playersOrderByPoints.map(function (pl) {
	        var r = {
	            activator: function (selected, selectItem, idx) {
	                return React.createElement("span", {key: idx}, React.createElement("button", {onClick: function (ev) { return selectItem(); }}, pl.player.name));
	            },
	            content: function () {
	                return (React.createElement("div", null, React.createElement(PlayerDeck_1.PlayerDeckSummaryCards, {cards: pl.score.cards}), React.createElement("div", {className: cssClasses_1.default.playerDeckSummary}, React.createElement("span", null, pl.points))));
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
	                return React.createElement("span", {key: idx}, React.createElement("button", {onClick: function (ev) { return selectItem(); }}, teamScore.teamName, " : ", teamScore.players.map(function (pl) { return pl.name; }).join(", ")));
	            },
	            content: function () {
	                return (React.createElement("div", null, React.createElement(PlayerDeck_1.PlayerDeckSummaryCards, {cards: teamScore.cards}), React.createElement("div", {className: cssClasses_1.default.playerDeckSummary}, React.createElement("span", null, total))));
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
/* 50 */
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
	        return (React.createElement("div", {className: props.classes && props.classes.container || ""}, React.createElement("div", {className: props.classes && props.classes.mainArea || ""}, mainArea), React.createElement("div", {className: props.classes && props.classes.activatorsContainer || ""}, activators)));
	    };
	    return TabPane;
	}(React.Component));
	exports.TabPane = TabPane;


/***/ },
/* 51 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var flib_1 = __webpack_require__(5);
	var Model = __webpack_require__(14);
	var cards_1 = __webpack_require__(47);
	var cssClasses_1 = __webpack_require__(46);
	/*
	function playerDeckSummaryCards(cards:Model.Card[]):React.ReactElement<any>[] {
	  const cardsBySeed = Arrays.groupBy(cards, c => Model.Seed[c.seed])
	  return Object.keys(cardsBySeed).map( (seed, idx) => {
	    const cards = cardsBySeed[seed];
	    cards.sort( (ca, cb) => {
	      const r = cb.points - ca.points
	      return (r !== 0) ? r : (cb.number - ca.number);
	    });
	    const total = Arrays.foldLeft(cards, 0, (acc, card) => acc + card.points);
	    return (
	      <div key={idx} className={cssClasses.playerDeckSummary}>
	        <span>{total}</span>
	        {cards.map( (card, idx) => <Card key={idx} card={card} /> )}
	      </div>
	    );
	  })
	}
	*/
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
	            return (React.createElement("div", {key: idx, className: cssClasses_1.default.playerDeckSummary}, React.createElement("span", null, total), cards.map(function (card, idx) { return React.createElement(cards_1.Card, {key: idx, card: card}); })));
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
	        var cardsBySeed = flib_1.Arrays.groupBy(props.cards, function (c) { return Model.Seed[c.seed]; });
	        var total = flib_1.Arrays.foldLeft(props.cards, 0, function (acc, card) { return acc + card.points; });
	        return (React.createElement("div", null, React.createElement("div", {className: cssClasses_1.default.playerDeckLayer}, React.createElement(PlayerDeckSummaryCards, {cards: props.cards}), React.createElement("div", null, React.createElement("span", null, total)), React.createElement("div", null, React.createElement("button", {onClick: props.onClose}, "Close")))));
	    };
	    return PlayerDeckSummary;
	}(React.Component));
	exports.PlayerDeckSummary = PlayerDeckSummary;


/***/ },
/* 52 */
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
	        return (React.createElement("div", {className: "{cssClasses.createPlayer} my-exp-style"}, React.createElement("input", {ref: PlayerLogin.playerNameRef, type: "text"}), React.createElement("input", {ref: PlayerLogin.playerPasswordRef, type: "password"}), React.createElement("input", {type: "button", onClick: function (e) { return props.onCreatePlayer(_this.playerName().value, _this.playerPassword().value); }, value: "Create Player"}), React.createElement("input", {type: "button", onClick: function (e) { return props.onPlayerLogin(_this.playerName().value, _this.playerPassword().value); }, value: "Log in"})));
	    };
	    PlayerLogin.playerNameRef = "playerName";
	    PlayerLogin.playerPasswordRef = "playerPasswordRef";
	    return PlayerLogin;
	}(React.Component));
	exports.PlayerLogin = PlayerLogin;


/***/ },
/* 53 */
/***/ function(module, exports, __webpack_require__) {

	"use strict";
	var __extends = (this && this.__extends) || function (d, b) {
	    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
	    function __() { this.constructor = d; }
	    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
	};
	var cssClasses_1 = __webpack_require__(46);
	/*
	import {Card, EmptyCard, CardBack} from "./cards"
	import {TabPane, TabPaneItem} from "./TabPane"
	import {EventsLog} from "./EventsLog"
	import {GameResult} from "./GameResult"
	import {PlayerDeckSummary} from "./PlayerDeck"
	import {PlayerLogin} from "./PlayerLogin"
	*/
	var CurrentPlayer = (function (_super) {
	    __extends(CurrentPlayer, _super);
	    function CurrentPlayer() {
	        _super.apply(this, arguments);
	    }
	    CurrentPlayer.prototype.render = function () {
	        var player = this.props;
	        return (React.createElement("div", {className: cssClasses_1.default.currentPlayer}, React.createElement("h5", null, "Current player: "), React.createElement("h4", null, player.name)));
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
	            return React.createElement("div", {key: idx}, React.createElement("input", {type: "checkbox", onChange: function (e) { return _this.props.onPlayerSelection(pl, !(selectedPlayers[pl.self] === true)); }, defaultChecked: selectedPlayers[pl.self]}), React.createElement("span", null, pl.name));
	        });
	        return (React.createElement("div", {className: cssClasses_1.default.players}, playersElems));
	    };
	    return Players;
	}(React.Component));
	exports.Players = Players;


/***/ },
/* 54 */
/***/ function(module, exports) {

	// removed by extract-text-webpack-plugin

/***/ }
/******/ ]);
//# sourceMappingURL=ddd-briscola-view.browser.js.map