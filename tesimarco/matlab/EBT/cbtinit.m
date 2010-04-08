function [k,i,L,pe]=cbtinit(A,k,t,i)
% t=0 i=1; per partire con il cbt
% k ultimo nodo visitato dal cbt
% i ultimo livello visitato

if (t>1)
    error('t deve essere minore di 1');
end

if (length(A)<2)
    error('Nessuna collisione');
end


%i
%t
x=2^-i;
pe=t+x;
a=A(k);
L=0;

%disp(['intervallo considerato : [' num2str(t) ' ' num2str(pe) ')']);

%a

if (pe>1)
    error('l''intervallo abilitato supera l''unità.')
end

if (k==length(A) && t<=a)
    pe=min(1,t+2*x);
    L=1;
    return;
end

if (a>=min(t+2*x,1))
    L=1;
    pe=1;
    return
end

b=A(k+1);
%b

if(b<=a)
    error('vettore A non ordinato o con valori dubplicati');
end

if(a>=t+2*x)
    % l'intervallo considerato (t,t+2x) è vuoto
    pe=min(t+2*x,1);
    L=1;
    % nessuna collisione i rimane uguale
    return
end

if(a>=t && b>=t+2*x)
    % l'intervallo considerato (t,t+2x) ha un solo nodo
    pe=t+2*x;
    L=1;
    % nessuna collisione i rimane uguale
    return
end

if (b<t)
    error('questo non dovrebbe succedere')
end

if (b<t+x)
    % collisione nella prima metà
 %   disp('a e b < t+x');
    [k,i,c,pe]=cbtinit(A,k,t,i+1);
    L=c+L+1;
elseif(a<t+x && b<t+2*x)
    % ora a e b sono in 2 metà separate
    % eventuali conflitti possono interessare b
  %  disp('a sarà risolto in questo slot');
    L=L+1;
   % disp('a è risolto (SX)');
    if (k+1<length(A) && A(k+2)<t+2*x)
    %    disp('risolvo conflitti b (DX)');
        [k,i,c,pe]=cbtinit(A,k+1,t+x,i);
        L=L+c;
    else
     %   disp('b non presenta conflitti (DX)');
        L=L+1;
        k=k+1;
        pe=t+2*x;
    end
    
elseif (a>=t+x)
    %disp('slot vuoto,a e b sono a DX');
    [k,i,c,pe]=cbtinit(A,k,t+x,i+1);
    L=c+L+1;
end