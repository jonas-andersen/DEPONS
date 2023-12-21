package dk.au.bios.porpoise.energetics;

public class ReimplementationCheck {

	
	void produce() {
/*
to reimplementation-check ; This needs to be called in the go procedure after energy intake

  ; number of values to gather for each output
  let n-recs 25

  ;;; Maintenance
  if maintenance-check = 0 [
    if length m-c-1 < n-recs [ if any? porps with [weight <= 20] [ ask one-of porps with [weight <= 20] [ set m-c-1 lput m-BMR m-c-1 ]]]
    if length m-c-2 < n-recs [ if any? porps with [weight > 20 and weight <= 40] [ ask one-of porps with [weight > 20 and weight <= 40] [ set m-c-2 lput m-BMR m-c-2 ]]]
    if length m-c-3 < n-recs [ if any? porps with [weight > 40 and weight <= 60] [ ask one-of porps with [weight > 40 and weight <= 60] [ set m-c-3 lput m-BMR m-c-3 ]]]
    if length m-c-4 < n-recs [ if any? porps with [weight >= 60] [ ask one-of porps with [weight >= 60] [ set m-c-4 lput m-BMR m-c-4 ]]]

    if length m-c-1 >= n-recs and length m-c-2 >= n-recs and length m-c-3 >= n-recs and length m-c-4 >= n-recs [
      set maintenance-check (list mean m-c-1 mean m-c-2 mean m-c-3 mean m-c-4)
      set m-c-1 "✓"
      set m-c-2 "✓"
      set m-c-3 "✓"
      set m-c-4 "✓"
    ]
  ]

  ;;; thermoregulation - should just follow lookup table values

  ;;; Locomotion
  if locomotion-check = 0 [
    if length l-c-1 < n-recs [ if any? porps with [swim-speed <= 0.5] [ ask one-of porps with [swim-speed <= 0.5] [ set l-c-1 lput m-loco l-c-1 ]]]
    if length l-c-2 < n-recs [ if any? porps with [swim-speed > 0.5 and swim-speed <= 1.0] [ ask one-of porps with [swim-speed > 0.5 and swim-speed <= 1.0] [ set l-c-2 lput m-loco l-c-2 ]]]
    if length l-c-3 < n-recs [ if any? porps with [swim-speed > 1.0 and swim-speed <= 1.5] [ ask one-of porps with [swim-speed > 1.0 and swim-speed <= 1.5] [ set l-c-3 lput m-loco l-c-3 ]]]
    if length l-c-4 < n-recs [ if any? porps with [swim-speed >= 1.5] [ ask one-of porps with [swim-speed >= 1.5] [ set l-c-4 lput m-loco l-c-4 ]]]

    if length l-c-1 >= n-recs and length l-c-2 >= n-recs and length l-c-3 >= n-recs and length l-c-4 >= n-recs [
      set locomotion-check (list mean l-c-1 mean l-c-2 mean l-c-3 mean l-c-4)
      set l-c-1 "✓"
      set l-c-2 "✓"
      set l-c-3 "✓"
      set l-c-4 "✓"
    ]
  ]


  ;;; pregnancy
  ; PregnancyCosts
  if preg-costs-check = 0 [
    if length p-c-c-1 < n-recs [ if any? porps with [ds-mating > 0 and ds-mating <= 75] [ ask one-of porps with [ds-mating > 0 and ds-mating <= 75] [ set p-c-c-1 lput m-preg p-c-c-1 ]]]
    if length p-c-c-2 < n-recs [ if any? porps with [ds-mating > 75 and ds-mating <= 150] [ ask one-of porps with [ds-mating > 75 and ds-mating <= 150] [ set p-c-c-2 lput m-preg p-c-c-2 ]]]
    if length p-c-c-3 < n-recs [ if any? porps with [ds-mating > 150 and ds-mating <= 225] [ ask one-of porps with [ds-mating > 150 and ds-mating <= 225] [ set p-c-c-3 lput m-preg p-c-c-3 ]]]
    if length p-c-c-4 < n-recs [ if any? porps with [ds-mating >= 225] [ ask one-of porps with [ds-mating >= 225] [ set p-c-c-4 lput m-preg p-c-c-4 ]]]

    if length p-c-c-1 >= n-recs and length p-c-c-2 >= n-recs and length p-c-c-3 >= n-recs and length p-c-c-4 >= n-recs [
      set preg-costs-check (list mean p-c-c-1 mean p-c-c-2 mean p-c-c-3 mean p-c-c-4)
      set p-c-c-1 "✓"
      set p-c-c-2 "✓"
      set p-c-c-3 "✓"
      set p-c-c-4 "✓"
    ]
  ]

  ; PregnancyMasses
  if preg-mass-check = 0 [
    if length p-m-c-1 < n-recs [ if any? porps with [ds-mating > 0 and ds-mating <= 75] [ ask one-of porps with [ds-mating > 0 and ds-mating <= 75] [ set p-m-c-1 lput mass-f p-m-c-1 ]]]
    if length p-m-c-2 < n-recs [ if any? porps with [ds-mating > 75 and ds-mating <= 150] [ ask one-of porps with [ds-mating > 75 and ds-mating <= 150] [ set p-m-c-2 lput mass-f p-m-c-2 ]]]
    if length p-m-c-3 < n-recs [ if any? porps with [ds-mating > 150 and ds-mating <= 225] [ ask one-of porps with [ds-mating > 150 and ds-mating <= 225] [ set p-m-c-3 lput mass-f p-m-c-3 ]]]
    if length p-m-c-4 < n-recs [ if any? porps with [ds-mating >= 225] [ ask one-of porps with [ds-mating >= 225] [ set p-m-c-4 lput mass-f p-m-c-4 ]]]

    if length p-m-c-1 >= n-recs and length p-m-c-2 >= n-recs and length p-m-c-3 >= n-recs and length p-m-c-4 >= n-recs [
      set preg-mass-check (list mean p-m-c-1 mean p-m-c-2 mean p-m-c-3 mean p-m-c-4)
      set p-m-c-1 "✓"
      set p-m-c-2 "✓"
      set p-m-c-3 "✓"
      set p-m-c-4 "✓"
    ]
  ]


  ;;; lactation
  ; LactationCosts
  if lact-costs-check = 0 [
    if length l-c-c-1 < n-recs [ if any? porps with [dsg-birth > 0 and dsg-birth <= 60] [ ask one-of porps with [dsg-birth > 0 and dsg-birth <= 60] [ set l-c-c-1 lput m-lact l-c-c-1 ]]]
    if length l-c-c-2 < n-recs [ if any? porps with [dsg-birth > 60 and dsg-birth <= 120] [ ask one-of porps with [dsg-birth > 60 and dsg-birth <= 120] [ set l-c-c-2 lput m-lact l-c-c-2 ]]]
    if length l-c-c-3 < n-recs [ if any? porps with [dsg-birth > 120 and dsg-birth <= 180] [ ask one-of porps with [dsg-birth > 120 and dsg-birth <= 180] [ set l-c-c-3 lput m-lact l-c-c-3 ]]]
    if length l-c-c-4 < n-recs [ if any? porps with [dsg-birth >= 180] [ ask one-of porps with [dsg-birth >= 180] [ set l-c-c-4 lput m-lact l-c-c-4 ]]]

    if length l-c-c-1 >= n-recs and length l-c-c-2 >= n-recs and length l-c-c-3 >= n-recs and length l-c-c-4 >= n-recs [
      set lact-costs-check (list mean l-c-c-1 mean l-c-c-2 mean l-c-c-3 mean l-c-c-4)
      set l-c-c-1 "✓"
      set l-c-c-2 "✓"
      set l-c-c-3 "✓"
      set l-c-c-4 "✓"
    ]
  ]

  ; LactationMasses
  if lact-mass-check = 0 [
    if length l-m-c-1 < n-recs [ if any? porps with [dsg-birth > 0 and dsg-birth <= 60] [ ask one-of porps with [dsg-birth > 0 and dsg-birth <= 60] [ set l-m-c-1 lput mass-calf l-m-c-1 ]]]
    if length l-m-c-2 < n-recs [ if any? porps with [dsg-birth > 60 and dsg-birth <= 120] [ ask one-of porps with [dsg-birth > 60 and dsg-birth <= 120] [ set l-m-c-2 lput mass-calf l-m-c-2 ]]]
    if length l-m-c-3 < n-recs [ if any? porps with [dsg-birth > 120 and dsg-birth <= 180] [ ask one-of porps with [dsg-birth > 120 and dsg-birth <= 180] [ set l-m-c-3 lput mass-calf l-m-c-3 ]]]
    if length l-m-c-4 < n-recs [ if any? porps with [dsg-birth >= 180] [ ask one-of porps with [dsg-birth >= 180] [ set l-m-c-4 lput mass-calf l-m-c-4 ]]]

    if length l-m-c-1 >= n-recs and length l-m-c-2 >= n-recs and length l-m-c-3 >= n-recs and length l-m-c-4 >= n-recs [
      set lact-mass-check (list mean l-m-c-1 mean l-m-c-2 mean l-m-c-3 mean l-m-c-4)
      set l-m-c-1 "✓"
      set l-m-c-2 "✓"
      set l-m-c-3 "✓"
      set l-m-c-4 "✓"
    ]
  ]

  ;;; growth
  ; GrowthCosts
  if growth-costs-check = 0 [
    if length g-c-c-1 < n-recs [ if any? porps with [age <= 3] [ ask one-of porps with [age <= 3] [ set g-c-c-1 lput m-growth g-c-c-1 ]]]
    if length g-c-c-2 < n-recs [ if any? porps with [age > 3 and age <= 6] [ ask one-of porps with [age > 3 and age <= 6] [ set g-c-c-2 lput m-growth g-c-c-2 ]]]
    if length g-c-c-3 < n-recs [ if any? porps with [age > 6 and age <= 9] [ ask one-of porps with [age > 6 and age <= 9] [ set g-c-c-3 lput m-growth g-c-c-3 ]]]
    if length g-c-c-4 < n-recs [ if any? porps with [age >= 9] [ ask one-of porps with [age >= 9] [ set g-c-c-4 lput m-growth g-c-c-4 ]]]

    if length g-c-c-1 >= n-recs and length g-c-c-2 >= n-recs and length g-c-c-3 >= n-recs and length g-c-c-4 >= n-recs [
      set growth-costs-check (list mean g-c-c-1 mean g-c-c-2 mean g-c-c-3 mean g-c-c-4)
      set g-c-c-1 "✓"
      set g-c-c-2 "✓"
      set g-c-c-3 "✓"
      set g-c-c-4 "✓"
    ]
  ]

  ; GrowthMasses
  if growth-mass-check = 0 [
    if length g-m-c-1 < n-recs [ if any? porps with [age <= 3] [ ask one-of porps with [age <= 3] [ set g-m-c-1 lput weight g-m-c-1 ]]]
    if length g-m-c-2 < n-recs [ if any? porps with [age > 3 and age <= 6] [ ask one-of porps with [age > 3 and age <= 6] [ set g-m-c-2 lput weight g-m-c-2 ]]]
    if length g-m-c-3 < n-recs [ if any? porps with [age > 6 and age <= 9] [ ask one-of porps with [age > 6 and age <= 9] [ set g-m-c-3 lput weight g-m-c-3 ]]]
    if length g-m-c-4 < n-recs [ if any? porps with [age >= 9] [ ask one-of porps with [age >= 9] [ set g-m-c-4 lput weight g-m-c-4 ]]]

    if length g-m-c-1 >= n-recs and length g-m-c-2 >= n-recs and length g-m-c-3 >= n-recs and length g-m-c-4 >= n-recs [
      set growth-mass-check (list mean g-m-c-1 mean g-m-c-2 mean g-m-c-3 mean g-m-c-4)
      set g-m-c-1 "✓"
      set g-m-c-2 "✓"
      set g-m-c-3 "✓"
      set g-m-c-4 "✓"
    ]
  ]

  ;;; storage
  ; StorageLevelAge
  if storage-level-age-check = 0 [
    if length sl-a-c-1 < n-recs [ if any? porps with [age <= 1] [ ask one-of porps with [age <= 1] [ set sl-a-c-1 lput storage-level sl-a-c-1 ]]]
    if length sl-a-c-2 < n-recs [ if any? porps with [age > 1 and age <= 2] [ ask one-of porps with [age > 1 and age <= 2] [ set sl-a-c-2 lput storage-level sl-a-c-2 ]]]
    if length sl-a-c-3 < n-recs [ if any? porps with [age > 2 and age <= 3] [ ask one-of porps with [age > 2 and age <= 3] [ set sl-a-c-3 lput storage-level sl-a-c-3 ]]]
    if length sl-a-c-4 < n-recs [ if any? porps with [age >= 3] [ ask one-of porps with [age >= 3] [ set sl-a-c-4 lput storage-level sl-a-c-4 ]]]

    if length sl-a-c-1 >= n-recs and length sl-a-c-2 >= n-recs and length sl-a-c-3 >= n-recs and length sl-a-c-4 >= n-recs [
      set storage-level-age-check (list mean sl-a-c-1 mean sl-a-c-2 mean sl-a-c-3 mean sl-a-c-4)
      set sl-a-c-1 "✓"
      set sl-a-c-2 "✓"
      set sl-a-c-3 "✓"
      set sl-a-c-4 "✓"
    ]
  ]

  ; StorageLevelMonth
  if storage-level-month-check = 0 [
    if length sl-m-c-1 < n-recs [ if month = 3 [ ask one-of porps [ set sl-m-c-1 lput storage-level sl-m-c-1 ]]]
    if length sl-m-c-2 < n-recs [ if month = 6 [ ask one-of porps [ set sl-m-c-2 lput storage-level sl-m-c-2 ]]]
    if length sl-m-c-3 < n-recs [ if month = 9 [ ask one-of porps [ set sl-m-c-3 lput storage-level sl-m-c-3 ]]]
    if length sl-m-c-4 < n-recs [ if month = 12 [ ask one-of porps [ set sl-m-c-4 lput storage-level sl-m-c-4 ]]]

    if length sl-m-c-1 >= n-recs and length sl-m-c-2 >= n-recs and length sl-m-c-3 >= n-recs and length sl-m-c-4 >= n-recs [
      set storage-level-month-check (list mean sl-m-c-1 mean sl-m-c-2 mean sl-m-c-3 mean sl-m-c-4)
      set sl-m-c-1 "✓"
      set sl-m-c-2 "✓"
      set sl-m-c-3 "✓"
      set sl-m-c-4 "✓"
    ]
  ]


  ;;; energy intake
  ; IntakeAge
    if intake-age-check = 0 [
    if length ei-a-c-1 < n-recs [ if any? porps with [age <= 3 and e-assim > 0] [ ask one-of porps with [age <= 3 and e-assim > 0] [ set ei-a-c-1 lput e-assim ei-a-c-1 ]]]
    if length ei-a-c-2 < n-recs [ if any? porps with [age > 3 and age <= 6 and e-assim > 0] [ ask one-of porps with [age > 3 and age <= 6 and e-assim > 0] [ set ei-a-c-2 lput e-assim ei-a-c-2 ]]]
    if length ei-a-c-3 < n-recs [ if any? porps with [age > 6 and age <= 9 and e-assim > 0] [ ask one-of porps with [age > 6 and age <= 9 and e-assim > 0] [ set ei-a-c-3 lput e-assim ei-a-c-3 ]]]
    if length ei-a-c-4 < n-recs [ if any? porps with [age >= 9 and e-assim > 0] [ ask one-of porps with [age >= 9 and e-assim > 0] [ set ei-a-c-4 lput e-assim ei-a-c-4 ]]]

    if length ei-a-c-1 >= n-recs and length ei-a-c-2 >= n-recs and length ei-a-c-3 >= n-recs and length ei-a-c-4 >= n-recs [
      set intake-age-check (list mean ei-a-c-1 mean ei-a-c-2 mean ei-a-c-3 mean ei-a-c-4)
      set ei-a-c-1 "✓"
      set ei-a-c-2 "✓"
      set ei-a-c-3 "✓"
      set ei-a-c-4 "✓"
    ]
  ]

  ; IntakeMonth
    if intake-month-check = 0 [
    if length ei-m-c-1 < n-recs [ if month = 3 [ if any? porps with [e-assim > 0] [ ask one-of porps with [e-assim > 0] [ set ei-m-c-1 lput e-assim ei-m-c-1 ]]]]
    if length ei-m-c-2 < n-recs [ if month = 6 [ if any? porps with [e-assim > 0] [ ask one-of porps with [e-assim > 0] [ set ei-m-c-2 lput e-assim ei-m-c-2 ]]]]
    if length ei-m-c-3 < n-recs [ if month = 9 [ if any? porps with [e-assim > 0] [ ask one-of porps with [e-assim > 0] [  set ei-m-c-3 lput e-assim ei-m-c-3 ]]]]
    if length ei-m-c-4 < n-recs [ if month = 12 [ if any? porps with [e-assim > 0] [ ask one-of porps with [e-assim > 0] [  set ei-m-c-4 lput e-assim ei-m-c-4 ]]]]

    if length ei-m-c-1 >= n-recs and length ei-m-c-2 >= n-recs and length ei-m-c-3 >= n-recs and length ei-m-c-4 >= n-recs [
      set intake-month-check (list mean ei-m-c-1 mean ei-m-c-2 mean ei-m-c-3 mean ei-m-c-4)
      set ei-m-c-1 "✓"
      set ei-m-c-2 "✓"
      set ei-m-c-3 "✓"
      set ei-m-c-4 "✓"
    ]
  ]

  ; IntakeCalves
  if calf-intake-check = 0 [
    if length ei-c-c-1 < n-recs [ if any? porps with [dsg-birth >= 90 and dsg-birth <= 127 and e-assim-calf > 0] [ ask one-of porps with [dsg-birth > 90 and dsg-birth <= 127 and e-assim-calf > 0] [ set ei-c-c-1 lput e-assim-calf ei-c-c-1 ]]]
    if length ei-c-c-2 < n-recs [ if any? porps with [dsg-birth > 127 and dsg-birth <= 164 and e-assim-calf > 0] [ ask one-of porps with [dsg-birth > 127 and dsg-birth <= 164 and e-assim-calf > 0] [ set ei-c-c-2 lput e-assim-calf ei-c-c-2 ]]]
    if length ei-c-c-3 < n-recs [ if any? porps with [dsg-birth > 164 and dsg-birth <= 201 and e-assim-calf > 0] [ ask one-of porps with [dsg-birth > 164 and dsg-birth <= 201 and e-assim-calf > 0] [ set ei-c-c-3 lput e-assim-calf ei-c-c-3 ]]]
    if length ei-c-c-4 < n-recs [ if any? porps with [dsg-birth >= 201 and e-assim-calf > 0] [ ask one-of porps with [dsg-birth >= 201 and e-assim-calf > 0] [ set ei-c-c-4 lput e-assim-calf ei-c-c-4 ]]]

    if length ei-c-c-1 >= n-recs and length ei-c-c-2 >= n-recs and length ei-c-c-3 >= n-recs and length ei-c-c-4 >= n-recs [
      set calf-intake-check (list mean ei-c-c-1 mean ei-c-c-2 mean ei-c-c-3 mean ei-c-c-4)
      set ei-c-c-1 "✓"
      set ei-c-c-2 "✓"
      set ei-c-c-3 "✓"
      set ei-c-c-4 "✓"
    ]
  ]

  ;;; TotalEnergyExpenditure
  if total-expenditure-check = 0 [
    if length tee-c-1 < n-recs [ if any? porps with [age <= 3] [ ask one-of porps with [age <= 3] [ set tee-c-1 lput m-tot tee-c-1 ]]]
    if length tee-c-2 < n-recs [ if any? porps with [age > 3 and age <= 6] [ ask one-of porps with [age > 3 and age <= 6] [ set tee-c-2 lput m-tot tee-c-2 ]]]
    if length tee-c-3 < n-recs [ if any? porps with [age > 6 and age <= 9] [ ask one-of porps with [age > 6 and age <= 9] [ set tee-c-3 lput m-tot tee-c-3 ]]]
    if length tee-c-4 < n-recs [ if any? porps with [age >= 9] [ ask one-of porps with [age >= 9] [ set tee-c-4 lput m-tot tee-c-4 ]]]

    if length tee-c-1 >= n-recs and length tee-c-2 >= n-recs and length tee-c-3 >= n-recs and length tee-c-4 >= n-recs [
      set total-expenditure-check (list mean tee-c-1 mean tee-c-2 mean tee-c-3 mean tee-c-4)
      set tee-c-1 "✓"
      set tee-c-2 "✓"
      set tee-c-3 "✓"
      set tee-c-4 "✓"
    ]
  ]


end
 */
	}
	
}
