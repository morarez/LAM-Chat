%%%-------------------------------------------------------------------
%%% @author mortezaarezoomandan
%%% @copyright (C) 2021, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 10. Feb 2021 11:10
%%%-------------------------------------------------------------------
-module(server).
-author("mortezaarezoomandan").
-compile([debug_info]).

%% API
-export([start/0]).

start() -> spawn(fun init/0).

init() -> loop([]).

loop(Clients) ->
  %% convert the exit signals to normal msg
  process_flag(trap_exit, true),
  %% receive messages from other processes
  receive
  %%send available rooms to new user
    {From, newuser} ->
      From ! {rooms, getRooms(Clients)},
      loop(Clients);
  %% if someone joins:
    {From, connect, Room} ->
      %%link the process. read about this in here: https://learnyousomeerlang.com/errors-and-processes
      link(From),
      From ! {users, getUsers(filterByRoom(Clients, Room))},
      receive
        {From, done, Username} ->
          broadcast(join, filterByRoom(Clients, Room), {Username}),
          loop([{Room, Username, From} | Clients]);
        _ ->
          loop(Clients)
      end;

    {From, clientListen, User} ->
      loop(reset(Clients, User, From));

    %%ChatRoom Messaging
    {From, send, Msg, User, room, Room} ->
      broadcast(new_msg, filterByRoom(Clients, Room), {User, Msg}),
      loop(Clients);

  %%Direct Messaging
    {From, send, Msg, Sender, user, User} ->
      broadcast(new_msg, filterByUser(Clients, User), {Sender, Msg}),
      loop(Clients);

    %% if someone exits:
  %%redo this
    {'EXIT', From, _} ->
      broadcast(disconnect, Clients, {From}),
      loop(remove(From, Clients));
  %% pattern didn't match:
    _ ->
      loop(Clients)
  end.


reset([H|T], User, From) ->
  {Room, Username, _} = H,
  if
    User == Username ->
      [{Room, Username, From}|T];
    true ->
      reset([T|H], User, From)
  end.


filterByUser(Clients, User) ->
  filterByUser(Clients, User, []).

filterByUser([], _, Result) ->
  Result;

filterByUser([H|T], User, Result) ->
  {_, U, _} = H,
  if
    U == User ->
      filterByUser(T, User, [U|Result]);
    true ->
      filterByUser(T, User, Result)
  end.


getRooms(Clients)->
  getRooms(Clients, []).

getRooms([], Result) ->
  Result;

getRooms([H|T], Result) ->
  {Room, _, _} = H,
  getRooms(T, [Room|Result]).

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
  broadcast({info, User, " joined the chatroom."}, Clients);

broadcast(new_msg, Clients, {User, Msg}) ->
  broadcast({new_msg, User, Msg}, Clients);

broadcast(disconnect, Clients, {User}) ->
  broadcast({info, User, " left the chatroom."}, Clients).

broadcast(Msg, Clients) ->
  lists:foreach(fun({_,_, Pid}) -> Pid ! Msg end, Clients).


%%find(From, [H|T]) ->
%%  {_, User, Pid} = H,
%%  if
%%    Pid == From ->
%%      User;
%%    true ->
%%      find(From, T)
%%  end.


%%
%%find(From, [{_, User, Pid} | _]) when From == Pid ->
%%  User;
%%find(From, [_ | T]) ->
%%  find(From, T).

remove(From, Clients) ->
  %remove the client from the list. filters: https://learnyousomeerlang.com/higher-order-functions#maps-filters-folds
  lists:filter(fun({_, _, Pid}) -> Pid =/= From end, Clients).