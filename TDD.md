# TDD: BYOW (Infinite Perlin World with LLM Dungeon Master)

## Data
- **Seed (long):** The source of truth for all world generation.
- **Chunk Data:** 2D arrays of `TETile` representing 16x16 or 32x32 areas.
- **Persistence Layer:** 
    - `save.txt`: Stores the world seed, current player coordinates, inventory, and a delta-map of edited tiles.
    - `story_state.json`: Stores the current LLM-generated plot progression, NPC relationships, and world lore flags.
- **LLM Context:** Metadata about "structures" (dungeons, towns) encountered, used to prompt the NPC dialogue engine.

## Data Structures
- **Chunk Map (`Map<Point, Chunk>`):** A hash map acting as a cache for Perlin-generated chunks. This allows for an "infinite" feel while keeping memory usage low by offloading distant chunks.
- **TETile[][] (The Grid):** The standard 61B tile representation for the currently rendered view.
- **Graph (Adjacency List):** Used within generated structures (dungeons) to ensure path connectivity between rooms.
- **Priority Queue:** For the AI boss's pathfinding and the RL-lite decision loop (if implemented via an action-value table).

## Algorithms
- **Perlin/Simplex Noise Generation:** 
    - Uses the seed to generate a continuous 2D heightmap. 
    - Thresholds define Biomes: `height < 0.3` is Water, `0.3 - 0.7` is Grass/Sand, `> 0.7` is Mountain.
- **Lazy Chunk Loading:** 
    - As `player.x` or `player.y` approaches a chunk boundary, the `World` class generates the adjacent 8 chunks using the seed and Perlin noise coordinates.
- **Structure Injection:** 
    - Using a secondary hash (e.g., `Random(seed + chunkCoord)`), we determine if a chunk contains a "Structure". 
    - Structures are generated using a standard 61B room-and-hallway algorithm but constrained within the chunk.
- **LLM NPC Interaction:**
    - A `DialogueManager` sends the current "World State" (biomes visited, bosses defeated) to an LLM API.
    - The LLM returns JSON containing the NPC's next line and potential quest updates.
- **RL-Lite Boss AI:**
    - Bosses use a simple Q-Learning or heuristic-based search to intercept the player. 
    - **A* Search:** Used for basic navigation toward the player.

## Complexity
- **Chunk Generation:** $O(C^2)$ where $C$ is chunk size. Performed once per new chunk discovered.
- **Rendering:** $O(W \cdot H)$ for the visible window $W \times H$.
- **Pathfinding (A*):** $O(E \log V)$ where $V$ is the number of tiles in the chunk and $E$ is possible moves.
- **Memory:** $O(N \cdot C^2)$ where $N$ is the number of chunks cached in the `Chunk Map`.

## Questions
- **Open:** How to handle LLM latency? (Solution: Use async calls and a "thinking" bubble over NPCs).
- **Open:** How to prevent "Backwash" in Perlin water? (Solution: Proper noise octaves to ensure landmasses are connected).
- **Closed:** Should the infinite map be truly infinite? (Decision: Yes, using long coordinates, limited only by Java's `long` overflow).

## Diagram
(In the final PDF/Doc, a flowchart showing the `Player Movement` -> `Chunk Check` -> `Noise Gen` -> `Cache` -> `Render` loop).
