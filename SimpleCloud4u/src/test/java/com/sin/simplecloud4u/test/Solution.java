package com.sin.simplecloud4u.test;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

public class Solution {
    @Test
    public void testSolution() {
        uniquePathsIII(new int[][]{{1, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 2, -1}});
    }

    public int uniquePathsIII(int[][] grid) {
        int ans = 0, n = grid.length, m = grid[0].length;
        int[][] dp = new int[n + 1][m + 1];
        boolean[][] vis = new boolean[n][m];
        int x = 0, y = 0, ex = 0, ey = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (grid[i][j] == 1) {
                    x = i;
                    y = j;
                } else if (grid[i][j] == 2) {
                    ex = i;
                    ey = j;
                }
            }
        }
        dp[x][y] = 1;
        vis[x][y] = true;

        int[] mov = new int[]{1, 0, -1, 0, 1};
        Queue<int[]> que = new LinkedList<>();
        que.add(new int[]{x, y});
        while (!que.isEmpty()) {
            int[] now = que.poll();
            for (int i = 0; i < 4; i++) {
                int nx = now[0] + mov[i], ny = now[1] + mov[i + 1];
                if (nx >= 0 && nx < n && ny >= 0 && ny < m && grid[nx][ny] != -1) {
                    if (dp[nx][ny] == 0 && !vis[nx][ny]) {
                        que.add(new int[]{nx, ny});
                        vis[nx][ny] = true;
                    } else {
                        dp[now[0]][now[1]] += dp[nx][ny];
                    }
                }
            }
        }
        return dp[ex][ey];
    }
}