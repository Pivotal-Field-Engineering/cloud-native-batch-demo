#!/bin/sh
scp trades-loader/src/main/resources/data.csv scdf@sftp.gcp.winterfell.live:/home/scdf/trades-remote-files/trades-data-$(date "+%Y.%m.%d-%H.%M.%S").csv