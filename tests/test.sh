#!/bin/bash
# testing script for the library distributed server system

java ../build/classes/Server < server.in > server_log &
s_pid=$!
#java Server < server2.in &
#s2_pid=$!
java ../build/classes/Client < c1.in > c1.out &
c1_pid=$!
java ../build/classes/Client < c2.in > c2.out &
c2_pid=$!
sleep 5s
kill -9 $s_pid
#kill -9 $s2_pid
kill -9 $c1_pid
kill -9 $c2_pid

