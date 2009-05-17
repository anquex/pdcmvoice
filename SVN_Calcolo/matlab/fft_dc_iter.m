%  Algoritmo di Cooley-Tukey radix-2
%    - implementazione iterativa in-place
%    - calcolo twiddle factors valutazione diretta (direct call)
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
%   X  : FFT di x , vettore colonna

function a=fft_dc_iter(a,is)

ldn=log2(length(a));
n=length(a);
a= bitrevorder(a); 
for ldm=1:ldn
   m=2^ldm; %dimensione fft
   mh=m/2; 
   for j=0:mh-1
      e=exp(is*2*pi*i*j/m);
      for r=0:m:n-m
          u=a(r+1+j);
          v=a(r+1+j+mh)*e;
          a(r+1+j)=u+v;
          a(r+1+j+mh)=u-v;
      end
   end  
end