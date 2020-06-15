
import chess
import chess.engine
import sys

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
