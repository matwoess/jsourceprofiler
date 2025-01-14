## Runtime impact
To evaluate how much the inserted counter statements impact the run-time performance of a program,
we ran a few benchmarks of the [DaCapo Benchmark Suite](https://dacapobench.sourceforge.net/)
in three different configurations:

- "orig" — The original unmodified benchmark project without instrumentation
- "instr" — A version with counter-increment statements added to every code block
- "sync" — The benchmark with synchronized counters, using the `AtomicLongArray`

The following figure shows the average relative run-time overhead of seven benchmarks
programs compared to their un-instrumented version:

![Relative runtime overhead in the DaCapo benchmarks](/screenshots/runtime-impact.png)

Most benchmarks show only a relatively small slowdown to less than 200% run time.
The h2 program does not show any significant impact as most of its work in performed
in its derby database library, which is not instrumented.
The sunflow benchmark (multi-threaded CPU ray-tracing) is the opposite extreme,
showing a significant 10-fold run time impact when using synchronized counters.

For further analysis and details, see the thesis paper.
