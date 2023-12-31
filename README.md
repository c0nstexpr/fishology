<h1 style="text-align:center">
<img alt= "fishology logo" src="https://raw.githubusercontent.com/c0nstexpr/fishology/main/interact/src/main/resources/assets/fishology-interact/icon.png" width=200 height=200 />

[![build](https://github.com/c0nstexpr/fishology/actions/workflows/build.yml/badge.svg)](https://github.com/c0nstexpr/fishology/actions/workflows/build-and-test.yml)
[![wakatime](https://wakatime.com/badge/github/c0nstexpr/fishology.svg)](https://wakatime.com/badge/github/c0nstexpr/fishology)
</h1>

## Features
Mod mainly focuses on auto-fishing:

- Auto fishing.
  - Adjust caught item judgement error. The smaller the value, the
    more strict the judgement, but may more likely to miss the fish. Please set the value under 
    judging by client performance, network status, etc. Generally 0.1-1.0 would be fine.
- Notify on caught fish.
  - Configure notification level.
  - Configure notification message format, refer to
    [JDK Format String Syntax](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Formatter.html#syntax).
  - Configure list of loot items.
- Notify on hooked entity.
  - Configure notification level.
  - Configure notification message format.
- Discard unwanted loots.
  - Configure list of loot items.
  - By enabling this feature, one might encounter hooking discarded item entity when fishing. Add
    bubble column to push away the discarded may solve the problem.
- Fishing statistics store locally.
  - client command "/fishology stat".
  - Use print to view statistics in hud.
  - Use clear to clear statistics.

## License

This project is licensed under the MIT license. See the [LICENSE](LICENSE) file for details.
