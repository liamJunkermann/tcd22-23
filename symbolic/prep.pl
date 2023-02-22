numeral(0).
numeral(s(X)) :- numeral(X).

subset(_, []).
subset(_, [0]).
subset(X, [X]) :- subset(X, []).
% subset(X, [X]) :- decrList(X).
subset(s(X), [s(X)|Xs]) :- subset(X, Xs).

s --> as, cs.
as --> [].
as --> [a],as,[b].

cs --> [].
cs --> [b],cs,[c].
