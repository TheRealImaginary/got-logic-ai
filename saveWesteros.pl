%% Facts and Initial Situations.
:- [westeros].

%% Move Actions
move_action(move_left, 0, -1).
move_action(move_right, 0, 1).
move_action(move_up, -1, 0).
move_action(move_down, 1, 0).

%% Attack Actions
attack_action(attack, 0, -1).
attack_action(attack, 0, 1).
attack_action(attack, -1, 0).
attack_action(attack, 1, 0).

%% obstacle(Cell): true if `Cell` contains an Obstacle.
obstacle(Cell):-
	obstacle_location(Cell).

%% dragonStone(Cell): true if `Cell` contains a Dragon Stone.
dragonStone(Cell):-
	dragonStone_location(Cell).

%% Jon Successor State Axiom.
%% --------------------------
%% Initial State.
jon(Cell, s0):-
	jon_location(Cell).

%% Actions Effects.
jon(Cell, result(A, S)):-
	( empty(Cell, S) ; dragonStone(Cell) ),
	jon(JonCell, S),
	canMove(JonCell, Cell, A).

%% Persistence.
jon(Cell, result(A, S)):-
	jon(Cell, S),
	(
		(dragonStone(Cell), A = pickup) ;
		(whiteWalker(WWCell, S), canAttack(Cell, WWCell, (A, S)))
	).
%% --------------------------

%% Empty Cell Successor State Axiom.
%% ---------------------------------
%% Initial State.
empty(Cell, s0):-
	empty_location(Cell).

%% Actions Effects.
empty(Cell, result(A, S)):-
	(
		(jon(Cell, S), not(dragonStone(Cell)),
				(empty(DestCell, S) ; dragonStone(DestCell)),
				canMove(Cell, DestCell, A)) ;

		(whiteWalker(Cell, S),
				jon(JonCell, S),
				canAttack(JonCell, Cell, (A, S)))
	).

%% Persistence.
empty(Cell, result(A, S)):-
	empty(Cell, S),
	(
		(jon(JonCell, S), dragonStone(JonCell), A = pickup);
		(jon(JonCell, S), whiteWalker(WWCell, S), canAttack(JonCell, WWCell, (A, S)));
		(jon(JonCell, S), (empty(DestCell, S) ; dragonStone(DestCell)), canMove(JonCell, DestCell, A), Cell \= DestCell)
	).
%% ---------------------------------

%% whiteWalker(Cell, S): true if `Cell` contains a White Walker in situation `S`.
whiteWalker(Cell, S):-
	whiteWalker_location(Cell),
	not(jon(Cell, S)),
	not(empty(Cell, S)).


%% dragonGlasses(DG, S): true if Jon has `DG` Dragon Glasses in situation `S`.
%% ---------------------------------------------------------------------------
dragonGlasses(DG, s0):-
	dragonGlasses(DG).

dragonGlasses(DG, result(A, S)):-
	(A = pickup, maxDragonGlasses(DG)) ;
	(A = attack, dragonGlasses(DG1, S), DG is DG1 - 1).

dragonGlasses(DG, result(A, S)):-
	dragonGlasses(DG, S),
	(A = move_left; A = move_right; A = move_up; A = move_down).
%% ---------------------------------------------------------------------------

%% isValid(Cell): true if `Cell` is inside the grid borders.
isValid((R, C)):-
	R >= 0,
	numberOfRows(MAX_ROWS),
	R < MAX_ROWS,
	C >= 0,
	numberOfColumns(MAX_COLUMNS),
	C < MAX_COLUMNS.

%% canMove(CellA, CellB, A): true if Jon can move from `CellA` to `CellB`.
canMove((R, C), (R1, C1), A):-
	move_action(A, DX, DY),
	R1 is R + DX,
	C1 is C + DY,
	isValid((R1, C1)).

%% canAttack(CellA, CellB, A): true if Jon can attack `CellB` from `CellA`.
canAttack((R, C), (R1, C1), (A, S)):-
	dragonGlasses(DG, S),
	DG > 0, 
	attack_action(A, DX, DY),
	R1 is R + DX,
	C1 is C + DY,
	isValid((R1, C1)).

%% Max depth to search in Iterative Deepening.
maxDepthToSearch(15).

%% killedAllWhiteWalkers(S): true if at situation `S` all White Walker cells are Empty Cells.
killedAllWhiteWalkers(S):-
	foreach(whiteWalker(Cell, s0), empty(Cell, S)).

%% Query for generating a plan.
%% Performs Iterative Deepening starting from depth 1 till `maxDepthToSearch`.
savedWesteros(S):-
	savedWesteros(S, 1).

%% Iterative Deepening.
savedWesteros(S, Depth):-
	maxDepthToSearch(MAX_DEPTH),
	Depth =< MAX_DEPTH,
	format('Current Depth = ~p~n', [Depth]),
	call_with_depth_limit(killedAllWhiteWalkers(S), Depth, R),
	(R = depth_limit_exceeded -> (NewDepth is Depth + 1,
		format('Done Searching At Depth = ~p~n', [Depth]),
		savedWesteros(S, NewDepth)) ; true).