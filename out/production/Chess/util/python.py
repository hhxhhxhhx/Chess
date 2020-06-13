
import chess
import chess.engine
import sys

'''
engine = chess.engine.SimpleEngine.popen_uci("/home/roger/IdeaProjects/Chess/src/util/stockfish")

board = chess.Board()
print(board)
info = engine.analyse(board, chess.engine.Limit(time=0.1))
print("Score:", info["score"])
# Score: +20

board = chess.Board("r1bqkbnr/p1pp1ppp/1pn5/4p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 2 4")
info = engine.analyse(board, chess.engine.Limit(depth=20))
print(board)
print("Score:", info["score"])
# Score: #+1

print(engine.go(movetime=2000))

engine.quit()
'''
engine = chess.engine.SimpleEngine.popen_uci('/home/roger/IdeaProjects/Chess/src/util/stockfish')
board = chess.Board(sys.argv[1])
info = engine.analyse(board, chess.engine.Limit(time=0.1))
score = info['score'].__str__()
if score[0] == '#':
    print(sys.argv[1])
    if sys.argv[1].find(' b ') != -1:
        score = 'Black forced mate in ' + score[2]
    else:
        score = 'White forced mate in ' + score[2]
else:
    val = int(score[1:]) / 100
    score = score[0] + str(val)

print("Score:", score)
print("Move:", info['pv'][0].__str__())

engine.quit()
