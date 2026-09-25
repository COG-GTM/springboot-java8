FROM alpine:3.20
RUN apk add --no-cache iptables
COPY apply.sh /apply.sh
ENTRYPOINT ["/apply.sh"]
