#!/bin/sh
scp ratings-loader/src/main/resources/data.csv scdf@sftp.gcp.winterfell.live:/home/scdf/ratings-remote-files/ratings-data-$(date "+%Y.%m.%d-%H.%M.%S").csv