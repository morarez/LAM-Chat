%%%-------------------------------------------------------------------
%%% @author mortezaarezoomandan
%%% @copyright (C) 2021, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 16. Feb 2021 17:40
%%%-------------------------------------------------------------------
-module(server).
-author("mortezaarezoomandan").

-compile([debug_info]).

-behaviour(gen_server).

%% API
-export([start_link/0]).
-export([init/1,
  handle_call/3,
  handle_cast/2,
  handle_info/2,
  terminate/2,
  code_change/3]). % gen_server callbacks

start_link() ->
  gen_server:start_link({local,lamchat},?MODULE, [], []).

init([]) ->
  Clients = gen_server:call(lamchatbackup, {recover}),
  {ok, Clients}.

%% connecting to the server
handle_call({newuser}, _From, Clients) ->
  Reply =  {rooms, getRooms(Clients)},
  {reply, Reply, Clients};

%% connecting to the room
handle_call({From, connect, Room, Username}, _From, Clients) ->
  TakenNames = getUsers(filterByRoom(Clients, Room)),
  Found = find(TakenNames, Username),
  if
    Found ->
      Reply =  {taken, Username},
      {reply, Reply, Clients};
    true ->
      Reply =  {users, getUsers(filterByRoom(Clients, Room))},
      broadcast(join, filterByRoom(Clients, Room), {Username}),
      gen_server:call(lamchatbackup, {connect, From, Room, Username}),
      {reply, Reply, [{Room, Username, From} | Clients]}
  end;

%%
handle_call({From, clientListen, User, Room}, _From, Clients) ->
  NewClients = reset(Clients, User, From, Room),
  gen_server:call(lamchatbackup, {clientListen, From, User, Room}),
  {reply, {ok}, NewClients};

%% sending the msg to chatroom
handle_call({send, Msg, User, room, Room}, _From, Clients) ->
  broadcast(new_msg, remove(User,filterByRoom(Clients, Room)), {User, Msg}),
  {reply, {ok}, Clients};

%% sending the msg to direct
handle_call({send, Msg, Sender, user, User}, _From, Clients) ->
  %% we should fix this broadcast
  broadcast(new_msg, filterByUser(Clients, User), {Sender, Msg}),
  {reply, {ok}, Clients};

handle_call({exit, Room, User}, _From, Clients) ->
  NewClients = remove(User, Room, Clients),
  gen_server:call(lamchatbackup, {exit, Room, User}),
  broadcast(disconnect, filterByRoom(NewClients, Room), {User}),
  {reply, {interrupt}, NewClients};


handle_call(Request, _From, State) ->
  {ok, {error, "Unhandled Request", Request}, State}.

handle_cast(_Request, State) ->
  {noreply, State}.

handle_info(Info, State) ->

  {noreply, State}.

terminate(_Reason, _State) ->
  ok.

code_change(_OldVsn, State, _Extra) ->
  {ok, State}.


remove(User, Room, [H|T]) ->
  {Roomname, Username, _} = H,
  if
    (Username == User) and (Roomname == Room) ->
      T;
    true ->
      remove(User, T++[H])
  end.

remove(User, [H|T]) ->
  {_, Username, _} = H,
  if
    Username == User ->
      T;
    true ->
      remove(User, T++[H])
  end.

reset([H|T], User, From, Room) ->
  {Roomname, Username, _} = H,
  if
    (User == Username) and (Room == Roomname) ->
      [{Room, Username, From}|T];
    true ->
      reset(T++[H], User, From, Room)
  end.


filterByUser(Clients, User) ->
  filterByUser(Clients, User, []).

filterByUser([], _, Result) ->
  Result;

filterByUser([H|T], User, Result) ->
  {_, U, _} = H,
  if
    U == User ->
      filterByUser(T, User, [H|Result]);
    true ->
      filterByUser(T, User, Result)
  end.


getRooms(Clients)->
  getRooms(Clients, []).

getRooms([], Result) ->
  Result;

getRooms([H|T], Result) ->
  {Room, _, _} = H,
  Found = find(Result, Room),
  if
    Found == true ->
      getRooms(T, Result);
    true ->
      getRooms(T, [Room|Result])
  end.

find([], _) ->
  false;

find([H|T], Identifier) ->
  Existing = H,
  if
    Identifier == Existing ->
      true;
    true ->
      find(T, Identifier)
  end.


getUsers(Clients)->
  getUsers(Clients, []).

getUsers([], Result)->
  Result;

getUsers([H|T], Result)->
  {_, User, _} = H,
  getUsers(T, [User|Result]).

filterByRoom(L, Room) ->
  filterByRoom(L, Room, []).

filterByRoom([], _, Result) ->
  Result;

filterByRoom([H|T], Room, Result) ->
  {R, _, _} = H,
  if
    R == Room ->
      filterByRoom(T, Room, [H|Result]);
    true ->
      filterByRoom(T, Room, Result)
  end.


broadcast(join, Clients, {User}) ->
  broadcast({info, User, joined}, Clients);

broadcast(new_msg, Clients, {User, Msg}) ->
  broadcast({new_msg, User, Msg}, Clients);

broadcast(disconnect, Clients, {User}) ->
  broadcast({info, User, left}, Clients).

broadcast(Msg, Clients) ->
  lists:foreach(fun({_,_, Pid}) -> Pid ! Msg end, Clients).

