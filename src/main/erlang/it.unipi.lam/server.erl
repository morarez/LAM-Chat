%%%-------------------------------------------------------------------
%%% @author ahmed
%%% @copyright (C) 2021, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 16. Feb 2021 3:14 PM
%%%-------------------------------------------------------------------
-module(server).
-author("ahmed").
-compile([debug_info]).

%% API
-export([start/0]).

start() -> spawn(fun init/0).

init() -> loop([]),pong().

pong() ->
	receive
	{ping,From} ->
				From ! {pong},
				pong()
end.

loop(Clients) ->
  %% convert the exit signals to normal msg
  %% process_flag(trap_exit, true),
  %% receive messages from other processes
  receive
	   %%handle recovery
	%%{From,recovery,Room,User} ->
	%%	loop([{From,connect,Room,User}]);
  %%send available rooms to new user
    {From, newuser} ->
      From ! {rooms, getRooms(Clients)},
      loop(Clients);
  %% if someone joins:
    {From, connect, Room, Username} ->
      %%link the process. read about this in here: https://learnyousomeerlang.com/errors-and-processes
      link(From),
      TakenNames = getUsers(filterByRoom(Clients, Room)),
      Found = find(TakenNames, Username),
      if
        Found ->
          From ! {taken, Username},
          loop(Clients);
        true ->
          From ! {users, getUsers(filterByRoom(Clients, Room))},
          broadcast(join, filterByRoom(Clients, Room), {Username}),
          loop([{Room, Username, From} | Clients])
      end;

    {From, clientListen, User} ->
      loop(reset(Clients, User, From));

  %%ChatRoom Messaging
    {From, send, Msg, User, room, Room} ->
      broadcast(new_msg, remove(User,filterByRoom(Clients, Room)), {User, Msg}),
      loop(Clients);

  %%Direct Messaging
    {From, send, Msg, Sender, user, User} ->
      broadcast(new_msg, filterByUser(Clients, User), {Sender, Msg}),
      loop(Clients);

  %% if someone exits:
    {'EXIT', From, Room, User} ->
      NewClients = remove(User, Room, Clients),
      broadcast(disconnect, filterByRoom(NewClients, Room), {User}),
      loop(NewClients);
  %% pattern didn't match:
    _ ->
      loop(Clients)
  end.

%%%WHAT IF THE USER JUST LEAVES , WHAT ABOUT 'EXIT' (ATOM OR STRING)
%%%ADD THE COOL FEATURE OF SUSPEND
%%%what happens if the server doesn't receive the receiver address
%%%how about adding an admin

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

reset([H|T], User, From) ->
  {Room, Username, _} = H,
  if
    User == Username ->
      [{Room, Username, From}|T];
    true ->
      reset(T++[H], User, From)
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

