#!/usr/bin/env bash

if is_service_running; then
    stop_service
fi