% dft.m
%
%  Algoritmo per il calcolo della DFT secondo definizione 
%
%  INPUT
%    x : vettore complesso
%
%  OUTPUT
%
%   X  : DFT di x , vettore colonna

function X = dft(x)

[N,M] = size(x);
if M ~=1,   % porta in vettore colonna
  x = x';
  N = M;
end
% Calcolo DFT
X=zeros(N,1);
n = 0:N-1;
for k=0:N-1
  X(k+1) = exp(-j*2*pi*k*n/N)*x;
end