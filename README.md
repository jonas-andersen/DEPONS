# DEPONS
Simulating effects of disturbances on harbour porpoise populations.

This is the third public version of the DEPONS model for simulating
population effects of noise for the harbour porpoise. Version 3.2 differs
from earlier versions in making it possible to simulate the combined 
population impact of noise from ships, bycatch and pile drivings, where 
sound emission from ships is related to the speed, length and type of the 
ships. Ship movements are defined by the user (potentially based on 
AIS data). Transmission loss for ship noise is related to bathymetry 
and sediment type. Harbour porpoise fine scale movements have been 
re-calibrated calibrated based on animals tagged in the North Sea,
and their deterrence behaviour is related to both distance to ships and
received sound from pilings and ships. The model has been calibrated for
the North Sea and for the inner Danish waters, but can be adapted for
use elsewhere (see TRACE document for details).

The objective of DEPONS is to simulate how harbour porpoise population 
dynamics are affected by disturbances, and was first developed to 
simulate effects of pile-driving noise associated with construction 
of offshore wind farms. Now it facilitates studies of cumulative 
impacts of pile-driving noise and ship noise. The animals’
survival is directly related to their energy levels, and the population
dynamics are affected by noise through its impact on the animals’
foraging behaviour. By ensuring that the animals’ movement patterns,
space use and reactions to noise are realistic, the population dynamics
in the model have the same causal drivers in the model as in nature.

The model was developed as part of the DEPONS project (Disturbance
Effects of POrpoises in the North Sea) at Aarhus University, Denmark.
The extension that enabled simulations of responses to ships was part
of the SATURN project (EU Horizon 2020; GA 101006443 ). The code
was developed in Eclipse/Repast, and must be installed under this
framework. See http://www.depons.dk or contact Jacob Nabe-Nielsen
(jnn@ecos.au.dk) for more information.