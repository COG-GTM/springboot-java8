#!/usr/bin/env bash
# Lists resources still tagged Project=<project>,Track=<track>. The Resource Groups Tagging API
# index is eventually consistent: terminated instances, deleted volumes and deleted SG rules stay
# in it for up to an hour, so each ARN is re-checked against the owning service before it counts.
set -euo pipefail
REGION=${1:?region} PROJECT=${2:?project} TRACK=${3:?track}

echo "== tagged resources remaining (Project=$PROJECT, Track=$TRACK, region=$REGION) =="
mapfile -t ARNS < <(aws resourcegroupstaggingapi get-resources --region "$REGION" \
  --tag-filters "Key=Project,Values=$PROJECT" "Key=Track,Values=$TRACK" \
  --query 'ResourceTagMappingList[].ResourceARN' --output text | tr '\t' '\n' | sed '/^$/d')

live=0
for arn in "${ARNS[@]:-}"; do
  [ -z "$arn" ] && continue
  id=${arn##*/}
  case "$arn" in
    *:ec2:*:instance/*)
      state=$(aws ec2 describe-instances --region "$REGION" --instance-ids "$id" \
        --query 'Reservations[].Instances[].State.Name' --output text 2>/dev/null || echo gone)
      [ "$state" = terminated ] || [ "$state" = gone ] && { echo "stale index entry (instance $state): $arn"; continue; } ;;
    *:ec2:*:volume/*)
      aws ec2 describe-volumes --region "$REGION" --volume-ids "$id" >/dev/null 2>&1 || { echo "stale index entry (volume deleted): $arn"; continue; } ;;
    *:ec2:*:security-group-rule/*)
      aws ec2 describe-security-group-rules --region "$REGION" --security-group-rule-ids "$id" >/dev/null 2>&1 || { echo "stale index entry (rule deleted): $arn"; continue; } ;;
  esac
  echo "LIVE: $arn"
  live=$((live + 1))
done

if [ "$live" -eq 0 ]; then
  echo "clean: 0 tagged resources remain"
else
  echo "$live tagged resource(s) still exist"
  exit 1
fi
