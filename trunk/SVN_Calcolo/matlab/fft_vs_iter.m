%  Algoritmo di Cooley-Tukey radix-2
%    - implementazione iterativa in-place
%    - calcolo twiddle factors attraverso subverctor scaling
%
%  Descrizione: utilizzabile per calolare ftt/ifft, è necessario
%               scalare la ifft restituita per 1/N
%
%  INPUT
%    x : vettore complesso
%   is : segno dell'esponenziale (+1)  FFT
%                                (-1) IFFT
%
%  OUTPUT
%
%   X  : FFT di x

function a=fft_vs_iter(a,is)

W=zeros(1,length(a));

ldn=log2(length(a));
n=length(a);
% Subvector scaling
W(1)=1;  % omega 0
for k=1:ldn
    u=2^k*pi/n;      % omega^(2^k)
    w=exp(is*i*u);
    W(2^(k-1)+1:2^(k))=w*W(1:2^(k-1));
end

%bit-reversal dell'input
a= bitrevorder(a); 

for ldm=1:ldn
   m=2^ldm; %dimensione della fft 
   mh=m/2;  %gli elementi sono suddivisi in n/m gruppi di (m/2)*2 elementi 
   for j=0:mh-1
      e=W(j*n/m+1); %rimappo gli indici 
      for r=0:m:n-m 
          u=a(r+1+j);
          v=a(r+1+j+mh)*e;
          a(r+1+j)=u+v;
          a(r+1+j+mh)=u-v;
      end
   end  
end