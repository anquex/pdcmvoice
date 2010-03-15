function y=f4(x,a)

% y=f4(x,a)
% 
% input:
%     a: base
%     x: x-point
%
% implements the function to ingegrate to calculate phi

y=exp(-x).*(1.+x).*f3(a,x).*x.^-2;
