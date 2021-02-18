%%%-------------------------------------------------------------------
%%% @author mortezaarezoomandan
%%% @copyright (C) 2021, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 16. Feb 2021 17:40
%%%-------------------------------------------------------------------
-module(backup).
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
  gen_server:start_link({local,lamchatbackup},?MODULE, [], []).

init([]) ->
  {ok, []}.

%% connecting to the room
handle_call({connect, From, Room, Username}, _From, Clients) ->
      {reply, ok, [{Room, Username, From} | Clients]};

handle_call({recover}, _From, Clients) ->
  {reply, Clients, Clients};
%%
handle_call({clientListen, From, User, Room}, _From, Clients) ->
  NewClients = reset(Clients, User, From, Room),
  {reply, {ok}, NewClients};

handle_call({exit, Room, User}, _From, Clients) ->
  NewClients = remove(User, Room, Clients),
  {reply, {ok}, NewClients};


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

