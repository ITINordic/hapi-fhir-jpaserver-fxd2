#!/bin/bash
sudo sh -c 'apt -y update && apt -y upgrade'
sudo apt install -y lxd
export MYUSER=$(id -un)
sudo usermod --append --groups lxd $MYUSER
sudo apt install -y zfsutils-linux
newgrp - <<EONG
cat <<EOF | lxd init --preseed
config: {}
networks:
- config:
    ipv4.address: auto
    ipv6.address: auto
  description: ""
  managed: false
  name: lxdbr1
  type: ""
storage_pools:
- config:
    size: 5GB
  description: ""
  name: test
  driver: zfs
profiles:
- config: {}
  description: ""
  devices:
    eth0:
      name: eth0
      nictype: bridged
      parent: lxdbr1
      type: nic
    root:
      path: /
      pool: test
      type: disk
  name: default
cluster: null
EOF
echo 'Done'
EONG
