%  Algoritmo Split-Radix DIF
%    - implementazione iterativa
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

function x= splitradix(x,is)

error('non va');
n=length(x);
if n<=1 
    return;
end

y=imag(x);
x=real(x);
ldn=log2(n);
    
% L shape butterly
n2=2*n;
for k=1:ldn
    n2= n2 / 2;
    n4= n2 / 4;
    e = 2 * pi / n2;
    for j=0:n4-1
        a   =(j)*e;
        cc1 = cos(a);
        ss1 = sin(a);
        cc3 = cos(3*a);
        ss3 = sin(3*a);
        ix  = j;
        id  = 2*n2;
        while ix<n-1
            i0=ix;
            while i0 <n
                i1 = i0 + n4;
                i2 = i1 + n4;
                i3 = i2 + n4;
                
                r1      = x(i0+1) - x(i2+1);
                x(i0+1) = x(i0+1) + x(i2+1);
                r2      = x(i1+1) - x(i3+1);
                x(i1+1) = x(i1+1) + x(i3+1);
                
                s1      = y(i0+1) - y(i2+1);
                y(i0+1) = y(i0+1) + y(i2+1);
                s2      = y(i1+1) - y(i3+1);
                y(i1+1) = y(i1+1) + y(i3+1);
                
                s3 = r1-s2;
                r1 = r1+s2;
                s2 = r2-s1;
                r2 = r2+s1;
                
                x(i2+1) = r1 * cc1 - s2 * ss1;
                y(i2+1) =-s2 * cc1 + r1 * ss1;
                
                x(i3+1) = s3 * cc3 + r2 * ss3;
                y(i3+1) = r2 * cc3 - s3 * ss3;
                
                i0 = i0+id;
            end
            ix = 2 * id - n2 + j;
            id = 4 *id;
        end
    end
end

% lenght 2 butterfly
ix = 1;
id = 4;

while ix<=n
    for i0=ix-1:id:n-id
        i1 = i0+1;
        tmp= x(i0+1) + x(i1+1);
        x(i1+1) = x(i0+1) - x(i1+1);
        x(i0+1)=tmp;
        tmp = y(i0+1) + y(i1+1);
        y(i1+1) = y(i0+1) - y(i1+1);
        y(i0+1)=tmp;
    end
    
    ix = 2 * id -1;
    id = 4 * id;
end
%--
x
y
x= bitrevorder(x);
y= bitrevorder(y);

if (is)>0
    for j=2:n/2
        tmp=x(j);
        x(j)=x(n-j);
        x(n-j)=tmp;
    end
    for j=2:n/2
        tmp=y(j);
        y(j)=y(n-j);
        y(n-j)=tmp;
    end
end
x=x+i*y;