<h1 style="text-align:center">
<img alt= "fishology logo" src="https://raw.githubusercontent.com/c0nstexpr/fishology/main/interact/src/main/resources/assets/fishology-interact/icon.png" width=200 height=200 />

[![build](https://github.com/c0nstexpr/fishology/actions/workflows/build.yml/badge.svg)](https://github.com/c0nstexpr/fishology/actions/workflows/build-and-test.yml)
[![wakatime](https://wakatime.com/badge/github/c0nstexpr/fishology.svg)](https://wakatime.com/badge/github/c0nstexpr/fishology)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/fishology)](https://modrinth.com/mod/fishology)

</h1>

## Features

Mod mainly focuses on auto-fishing:

- Auto fishing.
    - Adjust caught item judgement error. The smaller the value, the
      more strict the judgement, but may more likely to miss the fish. Please set the value under
      judging by client performance, network status, etc. Generally 0.1-1.0 would be fine.
  - Auto recast and retry when failed.
  - Adjust force recast time. Usually recast happened when loot drops down in a suitable height. But
    mod may not find the loot every time. After retrieving, force recast if waiting for loot time
    exceeds.
- Notify on caught fish.
    - Configure notification level.
    - Configure notification message format, refer to
      [JDK Format String Syntax](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Formatter.html#syntax).
    - Configure list of loot items.
- Notify on hooked entity.
    - Configure notification level.
    - Configure notification message format.
- Fishing statistics store locally.
    - client command "/fishology stat".
    - Use print to view statistics in hud.
    - Use clear to clear statistics.
  - Use last to view latest fishing round statistics
- Bobber tracking.
    - Indicator shows whether bobber is in open water

## License

This project is licensed under the MIT license. See the [LICENSE](LICENSE) file for details.
