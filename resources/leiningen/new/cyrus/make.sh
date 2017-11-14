#!/usr/bin/env bash
set -euo pipefail
IFS=$'\t\n'

DB_IMAGE=postgres:9.6
DB_CONTAINER={{name}}-db

cmd_db() {
    db_port=${1:-5432}
    docker rm -fv $DB_CONTAINER ||  true
    docker run -dt --name $DB_CONTAINER \
        -p $db_port:5432 \
        $DB_IMAGE
    set +x
    echo ""
    echo "==== PRO TIPS ===="
    echo ""
    echo "In order to psql into the database, use:"
    echo " ./make.sh psql"
    echo ""
    echo "Import a dump into an empty db:"
    echo " ./make.sh dbimport <dumpfile>"
    echo ""
    echo "This will show all the database logs:"
    echo " ./make.sh dblogs"
    echo ""
}

cmd_dbimport() {
    local dump_file=$1
    docker exec -i $DB_CONTAINER psql -U postgres < $dump_file
}

cmd_psql() {
    docker exec -it "$DB_CONTAINER" psql -U postgres "$@"
}

cmd_dblogs() {
    docker logs "$DB_CONTAINER" -f
}

# For more examples check out https://github.com/dryewo/make-sh

# Print all defined cmd_
cmd_help() {
    compgen -A function cmd_
}

# Run multiple commands without args
cmd_mm() {
    for cmd in "$@"; do
        cmd_$cmd
    done
}

if [[ $# -eq 0 ]]; then
    echo Please provide a subcommand
    exit 1
fi

SUBCOMMAND=$1
shift

# Enable verbose mode
set -x
# Run the subcommand
cmd_${SUBCOMMAND} $@
