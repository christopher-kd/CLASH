#!/usr/bin/env bash
# Mount/unmount tmpfs over the CLASH temp-world dimensions folder.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORLD_DIR="$SCRIPT_DIR/../run/worlds/main/dimensions/clash"
SIZE="512M"

usage() {
    echo "Usage: $0 {mount|umount|status}"
    exit 1
}

do_mount() {
    if mountpoint -q "$WORLD_DIR" 2>/dev/null; then
        echo "Already mounted: $WORLD_DIR"
        exit 0
    fi
    mkdir -p "$WORLD_DIR"
    sudo mount -t tmpfs -o size="$SIZE",mode=0755,uid="$(id -u)",gid="$(id -g)" tmpfs "$WORLD_DIR"
    echo "Mounted tmpfs ($SIZE) at $WORLD_DIR"
}

do_umount() {
    if ! mountpoint -q "$WORLD_DIR" 2>/dev/null; then
        echo "Not mounted: $WORLD_DIR"
        exit 0
    fi
    sudo umount "$WORLD_DIR"
    echo "Unmounted $WORLD_DIR"
}

do_status() {
    if mountpoint -q "$WORLD_DIR" 2>/dev/null; then
        df -h "$WORLD_DIR"
    else
        echo "Not mounted: $WORLD_DIR"
    fi
}

case "${1:-}" in
    mount) do_mount ;;
    umount) do_umount ;;
    status) do_status ;;
    *) usage ;;
esac
