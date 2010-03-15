%sample script to calculate phi

clc;
clear all;
close all;

b=2 % 1<b<=2
phi=quadl(@(x)f4(x,b),0,40,1.e-9)/log(b) ;
phi

% Sample output
%
% b =
%     2
% phi =
%   0.914217701315935
