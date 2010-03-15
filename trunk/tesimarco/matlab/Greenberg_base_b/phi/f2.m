function y=f2(a,x,k)

% y = f2(a,x,k)
% 
% a term inside the product

c=a.^k;
y=(1-exp(-c.*x).*(1+c.*x));