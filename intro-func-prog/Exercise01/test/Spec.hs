{-# LANGUAGE StandaloneDeriving #-}

module Main where

import Test.HUnit
import Test.Framework as TF (defaultMain, testGroup, Test)
import Test.Framework.Providers.HUnit (testCase)
-- import Test.Framework.Providers.QuickCheck2 (testProperty)

import Ex01

main = defaultMain tests -- runs the tests

tests :: [TF.Test]
tests = [ testGroup "\n\nExercise 01 Tests (40 marks)\n"
            [ part1Tests
            , part2Tests
            , part3Tests
            , part4Tests
            ]
        ]


part1Tests :: TF.Test
part1Tests
 = testGroup "\nPart 1 - lower (10 marks)\n"
    [ testCase "lower up [2 marks]" (lower "DOWN" @?= "down")
    , testCase "lower null [2 marks]" (lower "" @?= "")
    , testCase "lower numbers [2 marks]" (lower "1234" @?= "1234")
    , testCase "lower single [2 marks]" (lower "Z" @?= "z")
    , testCase "lower mixed [2 marks]" (lower "1aB2cD" @?= "1ab2cd")
    ]

part2Tests :: TF.Test
part2Tests
 = testGroup "\nPart 2 - nth (10 marks)\n"
    [ testCase "1st degree [2 marks]" (nth 1 "degree" @?= 'd')
    , testCase "2nd degree [2 marks]" (nth 2 "degree" @?= 'e')
    , testCase "4th degree [2 marks]" (nth 4 "degree" @?= 'r')
    , testCase "7th degrees [2 marks]" (nth 7 "degrees" @?= 's')
    , testCase "1st among equals" (nth 1 [42] @?= 42)
    ]

part3Tests :: TF.Test
part3Tests
 = testGroup "\nPart 3 - commonPfx (10 marks)\n"
    [ testCase "common people [2 marks]"
        (commonPfx "common people li" "common people like me" @?= "common people li")
    , testCase "1st is null [1 mark]" (commonPfx [] [1,2] @?= [])
    , testCase "2nd is null [1 mark]" (commonPfx [1,2] [] @?= [])
    , testCase "1st is shorter [1 mark]" (commonPfx [1,2] [1,2,3,4] @?= [1,2])
    , testCase "2nd is shorter [1 mark]" (commonPfx [1,2,3] [1] @?= [1])
    , testCase "both are same [1 mark]" (commonPfx [1,2,3] [1,2,3] @?= [1,2,3])
    , testCase "both differ - same length [1 mark]"
        (commonPfx [1,2,3] [4,5,6] @?= [])
    , testCase "complicated [1 mark]"
        (commonPfx [[],"x","xy"] [[],"a"] @?= [[]])
    , testCase "complicated [1 mark]"
        (commonPfx [[1,2],[],[3],[4,5]] [[1,2],[],[3,4,5]] @?= [[1,2],[]])
    ]

part4Tests :: TF.Test
part4Tests
 = testGroup "\nPart 3 - runs (10 marks)\n"
    [ testCase "run rabbit run [3 marks]"
        ( runs [1,2,2,1,3,3,3,2,2,1,1,4]
          @?= [[1],[2,2],[1],[3,3,3],[2,2],[1,1],[4]] )
    , testCase "runs null [1 marks]" (runs [] @?= ([]::[[Int]]))
    , testCase "runs single [1 marks]" (runs [42] @?= [[42]])
    , testCase "runs singles [1 marks]" (runs [1,2,3] @?= [[1],[2],[3]])
    , testCase "runs double [1 marks]" (runs [42,42] @?= [[42,42]])
    , testCase "runs doubles [1 marks]" (runs [42,42,99,99] @?= [[42,42],[99,99]])
    , testCase "runs nulls [2 marks]" (runs [([]::[Int]),[],[]] @?= [[[],[],[]]])
    ]
