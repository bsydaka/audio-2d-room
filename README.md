# audio-2d-room

## Run the room planner

This Java program starts a small web server that serves the room planner to a
browser. Modern browsers do not run Java applets, so the interactive editor is
served as a web page by the Java program.

Requires JDK 17 or newer. From this directory, run:

```sh
java audio-2d-room.java
```

Then open [http://localhost:8080](http://localhost:8080). To use another port,
pass it as the first argument, for example `java audio-2d-room.java 9000`.

Drag furniture into the room, move items to arrange them, and use
the selection panel to rotate or remove them. Room dimensions can be changed
between 3 m and 12 m. **Save layout** stores the plan in that browser, and
**Export layout** downloads it as JSON.

Drag speaker presets (Front Left/Right, Center, Surround/Rear Left/Right,
Subwoofer, and Atmos Left/Right) into the room, or add generic speakers. Each
speaker has an editable label, sensitivity, input power, direction, and
projection width. The TV / Projector screen is available as furniture.
Furniture can be resized, recolored, and relabeled, or created from custom
dimensions. **Start sound waves** plays a soft test tone and animates waves from
all speakers.

The listening measurements estimate distance, horizontal angle, speaker
symmetry, distance balance, each speaker's travel time, arrival-time difference,
relative level versus 1 m, and estimated SPL. SPL is estimated from speaker
sensitivity and input power; it is not a measurement. These are simplified
estimates, not an acoustic simulation: reflections, obstacles, room response,
and material absorption are not modeled.