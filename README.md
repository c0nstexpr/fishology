<h1 style="text-align:center">
<img alt= "fishology logo" src="./interact/src/main/resources/assets/fishology-interact/icon.png" width=200 height=200 />

[![build](https://github.com/c0nstexpr/fishology/actions/workflows/build.yml/badge.svg)](https://github.com/c0nstexpr/fishology/actions/workflows/build-and-test.yml)
[![wakatime](https://wakatime.com/badge/github/c0nstexpr/fishology.svg)](https://wakatime.com/badge/github/c0nstexpr/fishology)
</h1>

Mod that mainly focuses on auto-fishing, implemented the following features:
- Auto fishing.
  - Adjust caught item judgement by position error.
- Notify on caught fish.
  - Configure notification level.
  - Configure notification message format, refer to
    [JDK Format String Syntax](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Formatter.html#syntax).
  - Configure list of loot items.
- Chat on hooked entity.
- Discard unneeded loots.
  - Configure list of loot items.
  - By enabling this feature, one might encounter hooking discarded item entity when fishing on 
    flat water surface. Add bubble column to push away the discarded may solve the problem.

## License
This project is licensed under the MIT license. See the [LICENSE](LICENSE) file for details.
