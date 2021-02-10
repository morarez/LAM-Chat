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

%% API
-export([start/0]).

start() -> spawn(fun init/0).

init() -> loop([]).

loop(Clients) ->
  %% convert the exit signals to normal msg
  process_flag(trap_exit, true),
  %% receive messages from other processes
  receive
    %% if someone joins:
    {From, connect, User} ->
      %%link the process. read about this in here: https://learnyousomeerlang.com/errors-and-processes
      link(From),
      broadcast(join, Clients, {User}),
      loop([{User, From} | Clients]);
    %% if someone sends a msg:
    {From, send, Msg} ->
      broadcast(new_msg, Clients, {find(From, Clients), Msg}),
      loop(Clients);
    %% if someone exits:
    {'EXIT', From, _} ->
      broadcast(disconnect, Clients, {find(From, Clients)}),
      loop(remove(From, Clients));
  %% pattern didn't match:
    _ ->
      loop(Clients)
  end.

broadcast(join, Clients, {User}) ->
  broadcast({info, User ++ " joined the chatroom."}, Clients);
broadcast(new_msg, Clients, {User, Msg}) ->
  broadcast({new_msg, User, Msg}, Clients);
broadcast(disconnect, Clients, {User}) ->
  broadcast({info, User ++ " left the chatroom."}, Clients).

broadcast(Msg, Clients) ->
  lists:foreach(fun({_, Pid}) -> Pid ! Msg end, Clients).

find(From, [{User, Pid} | _]) when From == Pid ->
  User;
find(From, [_ | T]) ->
  find(From, T).

remove(From, Clients) ->
  %remove the client from the list. filters: https://learnyousomeerlang.com/higher-order-functions#maps-filters-folds
  lists:filter(fun({_, Pid}) -> Pid =/= From end, Clients).