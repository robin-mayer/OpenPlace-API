# OpenPlace API

Autocomplete addresses easily.

## Setup

1. Download [docker-compose.yml](https://github.com/robin-mayer/OpenPlace-API/blob/development/docker-compose.yml)
2. Set ``API_KEY`` to a random string of your choice
3. Optionally: Set the amount of search results in as ``RESULT_SIZE``
3. Run `docker-compose up -d`
4. Access the API at `http://localhost:8080`
5. Get the download link of your favorite OSM extract from [GEOFABRIK](https://download.geofabrik.de/index.html)
6. Start the import job with ``POST http://localhost:8080/import`` and request header `API-Key=your_api_key` and a body of: `
{
    "downloadUrl": "https://download.geofabrik.de/europe/germany-latest.osm.pbf"
}
`
7. Check logs and wait for ""Import of https://download.geofabrik.de/europe/germany-latest.osm.pbf completed""


## Usage
Make a GET request to `http://localhost:8080/search` with query parameter `q`, a result limit parameter `limit` and request header `API-Key=your_api_key`:

`localhost:8080/search?q=Hauptstr. 5 Berlin&limit=5`