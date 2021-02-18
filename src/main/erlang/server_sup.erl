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
  %%RestartStrategy = one_for_one,
  %%MaxRestart = 3,
  %%MaxTime = 20,
  %%FlagStrategy = {RestartStrategy, MaxRestart, MaxTime},
  %%ChildSpec = {lamchat, {server1, start_link, []}, permanent, infinity, worker, [server1]},
  %%{ok, FlagStrategy, [ChildSpec]}.


  SupFlags = #{strategy => one_for_one,
  intensity => 1,
  period => 5},

%% Specify a child process, including a start function.
%% Normally the module my_worker would be a gen_server
%% or a gen_fsm.
  Child = #{id => server1,
  start => {server1, start_link, []}},

%% In this case, there is only one child.
  Children = [Child],

%% Return the supervisor flags and the child specifications
%% to the 'supervisor' module.
  {ok, {SupFlags, Children}}.