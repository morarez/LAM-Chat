%%%-------------------------------------------------------------------
%%% @author mortezaarezoomandan
%%% @copyright (C) 2021, <COMPANY>
%%% @doc
%%% @end
%%%-------------------------------------------------------------------
-module(server_sup).
-behaviour(supervisor).

-export([start_link/0, start_link_shell/0, init/1]).

start_link() ->
  supervisor:start_link({local, ?MODULE}, ?MODULE, []).

start_link_shell()->
  {ok, Pid} = supervisor:start_link({local, ?MODULE}, ?MODULE, []),
  unlink(Pid).

init([]) ->
  SupFlags = #{strategy => one_for_one,
  intensity => 3,
  period => 20},

  Child = #{id => server,
  start => {server, start_link, []},
    restart => permanent,   % optional
    shutdown => infinity, % optional
    type => worker,       % optional
    modules => [server]},

  Children = [Child],

  {ok, {SupFlags, Children}}.