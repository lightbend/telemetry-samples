#!/usr/bin/env bash

function copy_files {
    from="$1"
    to="$2"

    # -v verbose
    # -r recursive
    # -p preserve permissions
    # -h output numbers in a human-readable format
    # --progress show progress during transfer
    # --executability preserve executability
    # --times preserve times
    # --stats give some file-transfer stats
    rsync -v -r -p -h --progress --executability --times --stats "$from" "$to"
}

function sync_sample {
    repo="$1"
    folder_inside_repo="$2"
    destination="$3"

    # Use a tmp dir to clone repositories
    tmp_dir=$(mktemp -d -t telemetry-samples)

    clone_folder_name=$(echo "$repo" | awk -F'[/\.]' '{ print $3 }')
    clone_folder="$tmp_dir/$clone_folder_name"

    git clone "$repo" "$clone_folder"

    folder_to_sync="$clone_folder/$folder_inside_repo"
    copy_files "$folder_to_sync" "$destination"
}

sync_sample "git@github.com:akka/akka-platform-guide.git" \
    "docs-source/docs/modules/microservices-tutorial/examples/shopping-cart-service-scala/" \
    "akka/shopping-cart-scala"

sync_sample "git@github.com:akka/akka-platform-guide.git" \
    "docs-source/docs/modules/microservices-tutorial/examples/shopping-cart-service-java/" \
    "akka/shopping-cart-java"

sync_sample "git@github.com:lagom/lagom-samples.git" \
    "shopping-cart/shopping-cart-java/" \
    "lagom/shopping-cart-java"

sync_sample "git@github.com:lagom/lagom-samples.git" \
    "shopping-cart/shopping-cart-scala/" \
    "lagom/shopping-cart-scala"
