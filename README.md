# OpenPlace API

Autocomplete addresses easily.

## Setup

1. Download [docker-compose.yml](https://github.com/robin-mayer/OpenPlace-API/blob/development/docker-compose.yml)
2. Set ``API_KEY`` to a random string of your choice
3. Get the download link of your favorite OSM extract from [GEOFABRIK](https://download.geofabrik.de/index.html)

Then choose one of the two import methods below.

### Auto Import

1. Set ``IMPORT_AUTO_URL`` to the download link, e.g. `https://download.geofabrik.de/europe/germany-latest.osm.pbf`
2. Run `docker-compose up -d`
3. Access the API at `http://localhost:8080`
4. Check logs and wait for "Import of https://download.geofabrik.de/europe/germany-latest.osm.pbf completed"

The import starts automatically once the server is ready, but only if the address table is still empty. This means it will not run again on subsequent restarts once data has been imported.

### Manual Import

1. Leave ``IMPORT_AUTO_URL`` empty
2. Run `docker-compose up -d`
3. Access the API at `http://localhost:8080`
4. Start the import job with ``POST http://localhost:8080/import`` and request header `API-Key=your_api_key` and a body of: `
{
    "downloadUrl": "https://download.geofabrik.de/europe/germany-latest.osm.pbf"
}
`
5. Check logs and wait for "Import of https://download.geofabrik.de/europe/germany-latest.osm.pbf completed"


## Usage
Make a GET request to `http://localhost:8080/search` with query parameter `q`, a result limit parameter `limit` and request header `API-Key=your_api_key`:

`localhost:8080/search?q=Hauptstr. 5 Berlin&limit=5`