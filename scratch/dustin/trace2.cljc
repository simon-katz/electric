(ns dustin.trace2
  (:require [minitest :refer [tests]]))


(deftype Flow [ast])

(tests
  (def ast '(let [>p (nodeI)                                ; no inputs, one output edge, can still resume a value
                  >q (nodeI)
                  >control (nodeI)
                  >cross (bindI >control (fn foo [c]
                                           (case c :p >p :q >q)))
                  >z (fmapI vector #_#_>p >q >cross)]))

  ; server
  (def flow (->Flow ast))
  (def !log (atom []))
  (add-watch flow #(swap! !log conj))

  (directive! flow '(put >control :q))

  ; effects run in response to directive
  @!log := ['(pulse >control :q)
            ; bind nodes not tagged as such in trace, but both notations are reasonable
            '(pulse >cross >q) #_'(bind >cross >q)]

  (directive! flow '(put >p 1) '(put >q 2))

  @!log := [...
            '(pulse >p 1)
            '(pulse >q 2)
            '(pulse >z [2])]
  (def dag @flow)
  (viz dag) := '{:edges ...
                 :ast   ...
                 :binds ...
                 :vals  ...}

  ; client
  (def !log ['(pulse >control :q)
             '(pulse >cross >q)
             '(pulse >p 1)
             '(pulse >q 2)
             '(pulse >z [2])])
  (def flow-client (->Flow ast))
  (replay! flow @!log)
  (def dag @flow)
  (viz dag) := '{:edges ...
                 :ast   ...
                 :binds ...
                 :vals  ...}

  (= server-viz client-viz) := true
  ; If IDs are not symbolic, it doesn't matter






  ; Pluse is an outbound value in motion
  ; Put is the external public interface on writable source nodes
  ; its an ALIAS, its the same node, and impl is (pulse)
  ; pulse can be used to violate invariants (state out of sync with formula)

  @!trace := '[(pulse >control :q)

               ; these are wormhole outputs, from some other flow/trace
               ; only the ouput is visible here
               (pulse >p 1)
               (pulse >q 2)

               (pulse >cross 2)                             ; output at wormhole
               (pulse >z [2])]

  )



