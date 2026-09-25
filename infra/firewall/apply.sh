#!/bin/sh
# Applies /etc/firewall/rules.conf to the shared network namespace, then idles.
set -eu
RULES=${RULES:-/etc/firewall/rules.conf}
iptables -F INPUT
iptables -A INPUT -i lo -j ACCEPT
iptables -A INPUT -m conntrack --ctstate ESTABLISHED,RELATED -j ACCEPT
grep -Ev '^\s*(#|$)' "$RULES" | while read -r proto port _; do
  echo "firewall: allow $proto/$port"
  iptables -A INPUT -p "$proto" --dport "$port" -j ACCEPT
done
iptables -P INPUT DROP
echo "firewall: default policy DROP"
iptables -S INPUT
exec sleep infinity
