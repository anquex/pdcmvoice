% dft.m
%
%  Algoritmo per il calcolo della IDFT secondo definizione 
%
%  INPUT
%    x : vettore complesso
%
%  OUTPUT
%
%   X  : vettore colonna IDFT di x

function x = idft(X)
[N,M] = size(X);
if M ~=1,   % makes sure that x is a column vector
  X = X';
  N = M;
end
x=zeros(N,1);
n = 0:N-1;
for k=0:N-1
  x(k+1) = exp(j*2*pi*k*n/N)*X;
end
x=1/N*x;